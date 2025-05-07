package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends AuditableEntity {
    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    private String id;

    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String password;
    private Boolean isDeleted;
    private Boolean isActive=false;
    private Boolean isExpired;
    private Boolean isLocked=false;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime passwordUpdateDate;
private String registrationAndLoginToken;
    private String forgetPasswordToken;
@ManyToOne(cascade ={ CascadeType.REFRESH,CascadeType.MERGE})
@JoinColumn(name = "role")
 private Role role;
private Integer failLoginAttemptsCount=0;
@OneToMany(cascade = CascadeType.ALL)
List<Address>addresses=new ArrayList<>();
    public User(Address address, String email, String firstName, String middleName, String lastName, String password, Boolean isDeleted, Boolean isActive, Boolean isExpired, Boolean isLocked, LocalDateTime passwordUpdateDate, Role role) {
        this.email = email;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.password = password;
        this.isDeleted = isDeleted;
        this.isActive = isActive;
        this.isExpired = isExpired;
        this.isLocked = isLocked;
        this.passwordUpdateDate = passwordUpdateDate;
        this.role = role;
        this.addresses.add(address);
    }
}
