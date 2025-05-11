package ro.unibuc.hello.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ro.unibuc.hello.data.user.User;
import ro.unibuc.hello.data.user.UserDTO;
import ro.unibuc.hello.service.UserService;
import ro.unibuc.hello.data.loyalty.LoyaltyCardEntity;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    private final Counter uniqueUserCounter;
    private final Counter totalUserCounter;

    public UserController(MeterRegistry registry) {
        uniqueUserCounter = registry.counter("users.unique");
        totalUserCounter = registry.counter("users.total");
    }

    @PostMapping("/api/users")
    @ResponseBody
    public void postUser(@RequestBody UserDTO userDTO) {
        try {
            userService.saveUser(userDTO);
            uniqueUserCounter.increment(); // Incrementăm pentru utilizator unic
            totalUserCounter.increment();  // Incrementăm pentru total utilizatori
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user");
        }
    }

    @GetMapping("/api/users/{id}")
    @ResponseBody
    public User getUser(@PathVariable String id) {
        try {
            return userService.getUserById(id);
        } catch (Exception e) {
            if (e.getMessage().equals(HttpStatus.NOT_FOUND.toString())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service not available");
            }
        }
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public void deleteUserById(@PathVariable String id) {
        try {
            userService.deleteUserById(id);
            totalUserCounter.increment(-1); // Scădem din total la ștergere
        } catch (Exception e) {
            if (e.getMessage().equals(HttpStatus.NOT_FOUND.toString())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service not available");
            }
        }
    }

    @PostMapping("/api/users/{id}/loyalty-cards")
    @ResponseBody
    public LoyaltyCardEntity issueCardToUser(@PathVariable String id, 
                                           @RequestBody Map<String, String> requestBody) {
        try {
            LoyaltyCardEntity.CardType cardType = LoyaltyCardEntity.CardType.valueOf(requestBody.get("cardType"));
            return userService.issueCardToUser(id, cardType);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card type");
        } catch (Exception e) {
            if (e.getMessage().equals(HttpStatus.NOT_FOUND.toString())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service not available");
            }
        }
    }

    @GetMapping("/api/users/{id}/loyalty-cards")
    @ResponseBody
    public List<LoyaltyCardEntity> getUserCards(@PathVariable String id) {
        try {
            return userService.getUserCards(id);
        } catch (Exception e) {
            if (e.getMessage().equals(HttpStatus.NOT_FOUND.toString())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service not available");
            }
        }
    }
}