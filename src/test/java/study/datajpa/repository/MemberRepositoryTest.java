package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamJpaRepository teamJpaRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member foundMember = memberRepository.findById(savedMember.getId()).get();

        Assertions.assertThat(foundMember.getId()).isEqualTo(savedMember.getId());
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member foundMember1 = memberRepository.findById(member1.getId()).get();
        Member foundMember2 = memberRepository.findById(member2.getId()).get();
        Assertions.assertThat(foundMember1).isEqualTo(member1);
        Assertions.assertThat(foundMember2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        Assertions.assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        Assertions.assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> findMember = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(findMember.get(0).getUsername()).isEqualTo("AAA");
        assertThat(findMember.get(0).getAge()).isEqualTo(20);
        assertThat(findMember.size()).isEqualTo(1);
    }

    @Test
    public void findByUserQuery(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> findMember = memberRepository.findUser("AAA", 15);
        assertThat(findMember.get(0).getUsername()).isEqualTo("AAA");
        assertThat(findMember.get(0).getAge()).isEqualTo(20);
        assertThat(findMember.size()).isEqualTo(1);
    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamJpaRepository.save(team);

        Member member1 = new Member("AAA", 10);
        memberRepository.save(member1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> members = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : members) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> aaa = memberRepository.findListByUsername("AAA");
        Member aaa1 = memberRepository.findMemberByUsername("AAA");
        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("AAA");
    }

    @Test
    public void paging(){
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        List<Member> members = page.getContent();
        long totalCount = page.getTotalElements();

        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void slice(){
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Slice<Member> page = memberRepository.findByAgeSlice(age, pageRequest);

        List<Member> members = page.getContent();

        assertThat(members.size()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate(){
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);
        /*벌크 연산 후 영속성 컨텍스트 초기화, 인터페이스에서 clearAutomatically = true로 하면 아래 코드 자동 실행*/
//        em.flush();
//        em.clear();
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy(){
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        teamJpaRepository.save(teamA);
        teamJpaRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();
    }

    @Test
    public void queryHing(){
        Member member1 = memberRepository.save(new Member("member1", 10));
        memberRepository.save(member1);
        em.flush();
        em.clear();

        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

        em.flush();
    }

    @Test
    public void lock(){
        Member member1 = memberRepository.save(new Member("member1", 10));
        memberRepository.save(member1);
        em.flush();
        em.clear();

        List<Member> result = memberRepository.findLockByUsername("member1");
    }

    @Test
    public void callCustom(){
        memberRepository.findMemberCustom();
    }

    @Test
    public void specBasic(){
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void queryByExample(){
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team); //inner join 만 가능

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");
        Example<Member> example = Example.of(member, matcher);

        List<Member> result = memberRepository.findAll(example);

        assertThat(result.get(0).getUsername()).isEqualTo("m1");
    }

    @Test
    public void projections(){
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        List<UsernameOnly> result = memberRepository.findProjectionByUsername("m1");
        List<UsernameOnlyDto> result2 = memberRepository.findClassProjectionByUsername("m1", UsernameOnlyDto.class);
        //projection 대상이 root가 아니면 left join으로 nested type을 모두 가져옴.
        List<NestedClosedProjections> result3 = memberRepository.findClassProjectionByUsername("m1", NestedClosedProjections.class);
    }

    @Test
    public void nativeQuery(){
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        Member result = memberRepository.findByNativeQuery("m1");
        Page<MemberProjection> result2 = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
    }
}