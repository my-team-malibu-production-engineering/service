package ro.unibuc.hello.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.hello.data.loyalty.LoyaltyCardEntity;
import ro.unibuc.hello.data.loyalty.LoyaltyCardRepository;
import ro.unibuc.hello.data.user.User;
import ro.unibuc.hello.data.user.UserDTO;
import ro.unibuc.hello.data.user.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoyaltyCardRepository loyaltyCardRepository;

    @Mock
    private LoyaltyCardService loyaltyCardService;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;
    private User user;
    private LoyaltyCardEntity loyaltyCard;

    @BeforeEach
    void setUp() {
        // Setăm UserDTO cu valorile corecte conform definiției clasei
        userDTO = new UserDTO("John", "Doe");
        List<String> cardIds = new ArrayList<>();
        cardIds.add("card1");
        userDTO.setLoyaltyCardIds(cardIds);

        // Creăm un User care ar trebui să fie creat din UserDTO
        user = new User();
        user.setId("user1");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setLoyaltyCardIds(cardIds);

        // Configurăm cardul de fidelitate pentru teste
        loyaltyCard = new LoyaltyCardEntity();
        loyaltyCard.setId("card1");
        loyaltyCard.setUserId("user1");
        loyaltyCard.setCardType(LoyaltyCardEntity.CardType.GOLD);
    }

    @Test
    void saveUser_SavesAndReturnsId() {
        // Configurăm mock-ul să returneze userul nostru când repository.save este apelat
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId("user1"); // Simulăm setarea ID-ului
            return savedUser;
        });

        // Apelăm metoda testată
        String userId = userService.saveUser(userDTO);

        // Verificăm rezultatul
        assertEquals("user1", userId);
        
        // Verificăm că repository-ul a fost apelat corect
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_UserExists_ReturnsUser() throws Exception {
        // Configurăm mock-ul să returneze userul nostru pentru ID-ul specificat
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        // Apelăm metoda testată
        User result = userService.getUserById("user1");

        // Verificăm rezultatul
        assertNotNull(result);
        assertEquals("user1", result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        
        // Verificăm că repository-ul a fost apelat corect
        verify(userRepository).findById("user1");
    }

    @Test
    void getUserById_UserNotFound_ThrowsNotFound() {
        // Configurăm mock-ul să returneze empty pentru un ID inexistent
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Verificăm că metoda aruncă excepția corectă
        Exception exception = assertThrows(Exception.class, () ->
                userService.getUserById("nonexistent"));
        
        // Verificăm mesajul excepției
        assertEquals("404 NOT_FOUND", exception.getMessage());
        
        // Verificăm că repository-ul a fost apelat corect
        verify(userRepository).findById("nonexistent");
    }

    @Test
    void deleteUserById_UserExists_DeletesUserAndCards() throws Exception {
        // Configurăm mock-ul să returneze o listă cu un card pentru ID-ul utilizatorului
        when(loyaltyCardRepository.findByUserId("user1")).thenReturn(Collections.singletonList(loyaltyCard));
        
        // Mock-ăm metoda deleteCard pentru a evita excepții
        doNothing().when(loyaltyCardService).deleteCard("card1");
        doNothing().when(userRepository).deleteById("user1");

        // Apelăm metoda testată
        userService.deleteUserById("user1");

        // Verificăm că metodele mock au fost apelate corect
        verify(loyaltyCardRepository).findByUserId("user1");
        verify(loyaltyCardService).deleteCard("card1");
        verify(userRepository).deleteById("user1");
    }

    @Test
    void issueCardToUser_CallsLoyaltyCardService() throws Exception {
        // Configurăm mock-ul să returneze cardul nostru
        when(loyaltyCardService.issueCard("user1", LoyaltyCardEntity.CardType.GOLD)).thenReturn(loyaltyCard);

        // Apelăm metoda testată
        LoyaltyCardEntity result = userService.issueCardToUser("user1", LoyaltyCardEntity.CardType.GOLD);

        // Verificăm rezultatul
        assertNotNull(result);
        assertEquals("card1", result.getId());
        assertEquals("user1", result.getUserId());
        
        // Verificăm că serviciul a fost apelat corect
        verify(loyaltyCardService).issueCard("user1", LoyaltyCardEntity.CardType.GOLD);
    }

    @Test
    void getUserCards_CallsLoyaltyCardService() throws Exception {
        // Configurăm mock-ul să returneze o listă cu un card
        when(loyaltyCardService.getCardsByUser("user1")).thenReturn(Collections.singletonList(loyaltyCard));

        // Apelăm metoda testată
        List<LoyaltyCardEntity> result = userService.getUserCards("user1");

        // Verificăm rezultatul
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("card1", result.get(0).getId());
        assertEquals("user1", result.get(0).getUserId());
        
        // Verificăm că serviciul a fost apelat corect
        verify(loyaltyCardService).getCardsByUser("user1");
    }
}