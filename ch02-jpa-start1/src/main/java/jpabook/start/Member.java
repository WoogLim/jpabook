package jpabook.start;

import javax.persistence.*;  //**

/**
 * User: HolyEyE
 * Date: 13. 5. 24. Time: 오후 7:43
 */

/** @author LIM
 * 
 *  기본적인 매핑 어노테이션
 *  @Entity : 이 클래스를 매핑한다고 JPA에게 넘겨줌 @Entity가 사용된 클래스를 엔티티 클래스라 한다.
 *  @Table  : 엔티티 클래스에 매핑할 테이블 정보를 알려준다. 여기서는 name 속성을 사용해서 Member 엔티티를 MEMBER 테이블에 매핑했다. 
 *  		  이 어노테이션을 생략한 경우 클래스 이름을 테이블 이름으로 매핑한다.
 *  @Id     : 엔티티 클래스의 필드를 Primary Key에 매핑한다. 이 식별자를 식별자 필드라함.
 *  @Column : 필드를 컬럼에 매핑한다. name 속성을 사용해서 Member 엔티티의 username 필드를 MEMBER 테이블의 NAME 컬럼에 매핑했다.
 */

@Entity
@Table(name="MEMBER")
public class Member {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String username;

    // 필드 명을 컬럼명으로 매핑함. 대소문자를 구분하지 않는다는 가정하에 아래처럼 사용가능하다. 
    private Integer age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
