package com.company.ecommercebackend.api.controller.user;

import com.company.ecommercebackend.api.model.DataChange;
import com.company.ecommercebackend.model.Address;
import com.company.ecommercebackend.model.LocalUser;
import com.company.ecommercebackend.model.dao.AddressDAO;
import com.company.ecommercebackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private final AddressDAO addressDAO;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    public UserController(AddressDAO addressDAO, SimpMessagingTemplate simpMessagingTemplate, UserService userService) {
        this.addressDAO = addressDAO;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
    }

    @GetMapping("/{userId}/address")
    public ResponseEntity<Optional<Address>> getAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId)
    {
        if(!userService.userHasPermisssionToUser(user,userId))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(addressDAO.findUserById(userId));
    }

    @PutMapping("/{userId}/address")
    public ResponseEntity<Address> putAddress(
            @AuthenticationPrincipal LocalUser user, @PathVariable Long userId,
            @RequestBody Address address)
    {
        if(!userService.userHasPermisssionToUser(user,userId))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        address.setId(null);
        LocalUser refUser=new LocalUser();
        refUser.setId(userId);
        address.setUser(refUser);
        Address savedAddress=addressDAO.save(address);
        simpMessagingTemplate.convertAndSend("/topic/user/"+userId+"/address",
                new DataChange<>(DataChange.ChangeType.INSERT,address));
        return ResponseEntity.ok(savedAddress);
    }

    @PatchMapping("/{userId}/address/{addressId}")
    public ResponseEntity<Address> patchAddress(
            @AuthenticationPrincipal LocalUser user,@PathVariable Long userId,
            @PathVariable Long addressId,@RequestBody Address address)
    {
        if(!userService.userHasPermisssionToUser(user,userId))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if(address.getId()==addressId)
        {
            Optional<Address> opOriginalAddress=addressDAO.findUserById(addressId);
            if(opOriginalAddress.isPresent())
            {
                LocalUser originalUser=opOriginalAddress.get().getUser();
                if(originalUser.getId()==userId)
                {
                    address.setUser(originalUser);
                    Address savedAddress=addressDAO.save(address);
                    simpMessagingTemplate.convertAndSend("/topic/user/"+userId+"/address",
                            new DataChange<>(DataChange.ChangeType.UPDATE,address));
                    return ResponseEntity.ok(savedAddress);
                }
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
