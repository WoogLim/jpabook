package jpabook.start;

import javax.persistence.*;
import java.util.List;

/**
 * @author holyeye
 */
public class JpaMain {

    public static void main(String[] args) {

    	/** 1. 엔티티 매니저 팩토리 생성  
    	 * META-INF/persistence.xml 에서 이름이 persistence-unit 이름이 jpabook인 영속성 유닛을 찾아 엔티티 매니저 팩토리를 생성한다.
    	 * persistence.xml 설정 정보를 읽어 JPA를 동작시키기 위한 기반 객체를 만들고 JPA 구현체에 따라 DB 커넥션 풀도 생성한다. 즉 생산 비용이 높으므로
    	 * 엔티티 매니저 팩토리는 애플리케이션 전체에서 딱 한번만 생성하고 공유해서 사용해야 한다.
    	 * 여러 스레드에서 접근해도 문제가 없음.
    	 */
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        /** 2. 엔티티 매니저 생성
         * 엔티티 매니저 팩토리에서 엔티티 매니저를 생성. JPA의 기능 대부분이 이 엔티티가 제공한다.
         * 엔티티 매니저를 사용해서 엔티티를 데이터베이스에 등록/수정/삭제/조회할 수 있다.
         * 엔티티 매니저는 내부에서 데이터소스(DB 커넥션)을 유지하며 DB와 통신한다. 따라서 애플리케이션 개발자는 엔티티 매니저를 가상의 데이터베이스로 생각할 수 있다.
         * 또한 엔티티 매니저는 DB 커넥션과 밀접한 관계를 가지므로 스레드간에 공유하거나 재사용하면 안 된다. > 트랜잭션이 꼬이므로 DB 락이 발생할 수 있을거같음.
         */
        EntityManager em = emf.createEntityManager(); //엔티티 매니저 생성
        /**
         * DB 와 통신을 위한 트랜잭션 API
         * 엔티티 매니저로부터 트랜잭션을 받아온다.
         */
        EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득

        try {
            tx.begin(); // 트랜잭션 시작
            logic(em);  // 비즈니스 로직
            tx.commit();// 트랜잭션 커밋 : 영속성 컨텍스트내 변경 내용을 플러시 한 후 영속성 컨텍스트와 데이터베이스 저장. logic() 부분에서 지연쓰기 저장소에 저장 후 커밋시 실행한다.
            			// 플러시는 em.flush() 직접 호출, 커밋, JPQL쿼리 실행시 호출된다. 
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback(); //트랜잭션 롤백
        } finally {
            em.close(); //엔티티 매니저 종료
        }

        emf.close(); //엔티티 매니저 팩토리 종료
    }

    public static void logic(EntityManager em) {

    	/** [1] 엔티티 생명주기
    	 * [1-1] 비영속 상태 : 객체 상태이며 영속성 컨텍스트에 저장하지 않은 상태 new. 영속성 컨텍스트나 데이터베이스와 전혀 연관이 없다.
    	 */
        String id = "id1";
        Member member = new Member();
        member.setId(id);
        member.setUsername("지한");
        member.setAge(2);

        //등록

    	/** [1] 엔티티 생명주기
    	 * [1-2] 영속 상태 persist : 엔티티 매니저를 통해 엔티티를 영속성 컨텍스트에 저장. 해당 엔티티는 영속 상태임. 비영속 > 영속 반드시 식별자 값(PK)이 있어야한다.
    	 * 영속 상태에 접어들시 1차 캐시에 저장. 영속성 컨텍스트는 Map으로 구성되며 현재 엔티티의 id가 키. 값은 엔티티 인스턴스가 된다. 영속컨텍스트에 있을뿐, 데이터베이스에 저장은 아직 안한 상태
    	 * 영속성 컨텍스트에 저장/조회하는 모든 기준은 데이터베이스 기본키 값이다.
    	 * [1-3] 준영속 상태 : em.detach, em.close, em.clear 를 통해 영속성 컨텍스트에서 관리하지 않는 준영속 상태로 바꿀 수 있다.
    	 */
        /** 1. 엔티티 저장시 엔티티 매니저의 persist() 메소드에 저장할 엔티티를 넘겨주면 된다.
         *	JPA 는 회원 엔티티의 매핑 정보(어노테이션)를 분석해서 다음과 같은 SQL을 만들어 데이터베이스에 전달한다.
         *
         *  2. 조회 뿐만아닌 엔터티 정보를 영구적으로 저장하기 위해 회원 엔티티를 엔티티를 연구 저장하는 공간인 영속성 컨텍스트에 저장한다.
         *  영속성 컨텍스트는 엔티티 매니저 생성시 하나가 만들어진다. 앤티티 매니저를 통해 영속성 컨텍스트 접근하고 관리할 수 있다.
         */
        em.persist(member);

        //수정
        /** 매핑 정보의 수정사항을 모니터링해 수정이 발생하면 JPA에서 UPDATE를 수행한다.
         *  만일 영속성 컨텍스트내 최초 상태(스냅샷)와 비교해 변경된 에티티를 검색한다. 실제 SQL 문에서 age만 변경하는것이아닌, 모든 컬럼을 변경한다. 없는 경우 기존값이 들어가는것같음.
         *  필드가 많거나 저장 내용이 크다면 동적으로 UPDATE SQL를 생성하는 전략을 사용한다. 보통 컬럼이 30개 이상인 경우 @DynamicUpdate를 사용한다.
         */
        member.setAge(20);

        // 한 건 조회 조회할 엔티티 타입과 기본키 식별자로 하나를 조회한다. find 호출시 1차로 1차 캐시에 검색후 없다면 데이터베이스에서 조회해온다.
        // 영속성 컨텍스트에 저장된 엔티티는 동일성 및 동등성 특징을 지닌다. 실제 인스턴스는 다르더라도 값은 동일할 수 있고 키값으로 조회시 캐시에서 1차 불러오기 때문에 같은 주소를 참조해 동일성을 가진다.
        Member findMember = em.find(Member.class, id);
        System.out.println("findMember=" + findMember.getUsername() + ", age=" + findMember.getAge());

        //목록 조회 JPQL 이용. 엔티티 객체를 중심으로 쿼리한다. em.CreateQuery 메소드를 실행해서 쿼리 객체 생성후 쿼리 객체의 getResultList()를 호출하면 된다.
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        System.out.println("members.size=" + members.size());

        //삭제
        /** [1] 엔티티 생명주기
    	 * [1-4] 삭제 : 엔티티를 영속성 컨텍스트와 데이터베이스에서 삭제
    	 */
        em.remove(member);

        // logic 내 필드 : 영속성 컨텍스트에 1차로 저장, 모아놓은 쿼리를 쓰기지연 저장소에 저장해 commit시 실행한다.
    }
}
