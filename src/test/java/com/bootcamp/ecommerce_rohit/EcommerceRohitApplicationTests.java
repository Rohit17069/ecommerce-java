package com.bootcamp.ecommerce_rohit;

import com.bootcamp.ecommerce_rohit.entities.Role;
import com.bootcamp.ecommerce_rohit.entities.Seller;
import com.bootcamp.ecommerce_rohit.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class EcommerceRohitApplicationTests {
    @Autowired
    public RoleRepository roleRepository;

    @Test
    void contextLoads() {
    }

//    @Test
//    void addRole(){
//        Role role1 = new Role("customer");
//        Role role2 = new Role("seller");
//        Role role3 = new Role("admin");
//        roleRepository.save(role1);
//        roleRepository.save(role2);
//        roleRepository.save(role3);
//    }
}
