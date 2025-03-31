package ro.unibuc.hello.data.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserRepositoryTest {

    @Test
    public void testSimpleAssertions() {
        // Un test simplu care va trece întotdeauna
        assertTrue(true, "Acest test ar trebui să treacă întotdeauna");
        assertEquals(4, 2 + 2, "Verificare matematică de bază");
    }

    @Test
    public void testUserCreation() {
        // Test pentru crearea unui utilizator
        User user = new User();
        user.setId("user-id-1");
        user.setFirstName("Ana");
        user.setLastName("Popescu");
        
        // Verificăm că obiectul a fost creat corect
        assertEquals("user-id-1", user.getId());
        assertEquals("Ana", user.getFirstName());
        assertEquals("Popescu", user.getLastName());
        
        // Lista de carduri de fidelitate ar trebui să fie inițializată
        assertNotNull(user.getLoyaltyCardIds());
        assertTrue(user.getLoyaltyCardIds().isEmpty());
    }
    
    @Test
    public void testCreateFromUserDTO() {
        // Test pentru crearea unui utilizator din DTO
        UserDTO userDTO = new UserDTO("Gabriel", "Georgescu");
        
        // Creăm un utilizator din DTO
        User user = new User(userDTO);
        
        // Verificăm că utilizatorul a fost creat corect
        assertEquals("Gabriel", user.getFirstName());
        assertEquals("Georgescu", user.getLastName());
        assertNotNull(user.getLoyaltyCardIds());
        assertTrue(user.getLoyaltyCardIds().isEmpty());
    }
    
    @Test
    public void testAddLoyaltyCardId() {
        // Test pentru adăugarea unui card de fidelitate
        User user = new User();
        user.setFirstName("Ionut");
        user.setLastName("Ionescu");
        
        // Inițial, lista de carduri ar trebui să fie goală
        assertTrue(user.getLoyaltyCardIds().isEmpty());
        
        // Adăugăm un card
        user.addLoyaltyCardId("card-id-1");
        
        // Verificăm că a fost adăugat
        assertEquals(1, user.getLoyaltyCardIds().size());
        assertTrue(user.getLoyaltyCardIds().contains("card-id-1"));
        
        // Adăugăm încă un card
        user.addLoyaltyCardId("card-id-2");
        
        // Verificăm că avem două carduri
        assertEquals(2, user.getLoyaltyCardIds().size());
        assertTrue(user.getLoyaltyCardIds().contains("card-id-2"));
    }
    
    @Test
    public void testRemoveLoyaltyCardId() {
        // Test pentru eliminarea unui card de fidelitate
        User user = new User();
        
        // Adăugăm câteva carduri
        user.addLoyaltyCardId("card-id-1");
        user.addLoyaltyCardId("card-id-2");
        user.addLoyaltyCardId("card-id-3");
        
        // Verificăm că avem 3 carduri
        assertEquals(3, user.getLoyaltyCardIds().size());
        
        // Creăm o nouă listă fără unul dintre carduri
        List<String> updatedCardIds = new ArrayList<>(user.getLoyaltyCardIds());
        updatedCardIds.remove("card-id-2");
        
        // Setăm lista actualizată
        user.setLoyaltyCardIds(updatedCardIds);
        
        // Verificăm că avem 2 carduri și că card-id-2 a fost eliminat
        assertEquals(2, user.getLoyaltyCardIds().size());
        assertTrue(user.getLoyaltyCardIds().contains("card-id-1"));
        assertFalse(user.getLoyaltyCardIds().contains("card-id-2"));
        assertTrue(user.getLoyaltyCardIds().contains("card-id-3"));
    }
    
    @Test
    public void testLoyaltyCardIdsList() {
        // Test pentru setarea și obținerea listei de carduri
        User user = new User();
        
        // Setăm o listă de carduri
        List<String> cardIds = Arrays.asList("card-1", "card-2", "card-3");
        user.setLoyaltyCardIds(cardIds);
        
        // Verificăm că lista a fost setată corect
        assertEquals(3, user.getLoyaltyCardIds().size());
        assertTrue(user.getLoyaltyCardIds().containsAll(cardIds));
    }
    
    @Test
    public void testLoyaltyCardIdsInitialization() {
        // Test pentru a verifica că lista de carduri este inițializată corect
        User user = new User();
        
        // Lista ar trebui să fie inițializată (nu null)
        assertNotNull(user.getLoyaltyCardIds());
        
        // Lista ar trebui să fie goală
        assertTrue(user.getLoyaltyCardIds().isEmpty());
        
        // Setăm lista la null
        user.setLoyaltyCardIds(null);
        
        // Adăugăm un card - metoda ar trebui să reinițializeze lista
        user.addLoyaltyCardId("card-id");
        
        // Verificăm că lista a fost recreată
        assertNotNull(user.getLoyaltyCardIds());
        assertEquals(1, user.getLoyaltyCardIds().size());
        assertTrue(user.getLoyaltyCardIds().contains("card-id"));
    }
    
    @Test
    public void testUserDTO() {
        // Test pentru UserDTO
        UserDTO userDTO = new UserDTO("Maria", "Marinescu");
        
        // Verificăm proprietățile
        assertEquals("Maria", userDTO.getFirstName());
        assertEquals("Marinescu", userDTO.getLastName());
        
        // Lista de carduri ar trebui să fie null inițial
        assertNull(userDTO.getLoyaltyCardIds());
        
        // Setăm o listă de carduri
        List<String> cardIds = Arrays.asList("card-1", "card-2");
        userDTO.setLoyaltyCardIds(cardIds);
        
        // Verificăm că lista a fost setată
        assertNotNull(userDTO.getLoyaltyCardIds());
        assertEquals(2, userDTO.getLoyaltyCardIds().size());
        
        // Testăm constructorul gol
        UserDTO emptyDTO = new UserDTO();
        assertNull(emptyDTO.getFirstName());
        assertNull(emptyDTO.getLastName());
    }
}