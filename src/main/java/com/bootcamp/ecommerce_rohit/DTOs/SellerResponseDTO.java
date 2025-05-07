package com.bootcamp.ecommerce_rohit.DTOs;
import com.bootcamp.ecommerce_rohit.entities.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SellerResponseDTO {
    String  id;
    String fullName;
    String email;
    Boolean isActive;
String companyName;
Address companyAddress;
String companyContact;
}
