package ac.csg.pu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TenthOrderLogicTest {

    private boolean isTenthOrder(int currentCount) {
        return (currentCount + 1) % 10 == 0;
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    @Test
    void nineExistingOrdersMakesNextOneTenth() {
        assertTrue(isTenthOrder(9));
    }

    @Test
    void brandNewUserIsNotOnTenthOrder() {
        assertFalse(isTenthOrder(0));
    }

    @Test
    void nineteenExistingOrdersMakesNextOneTwentieth() {
        assertTrue(isTenthOrder(19));
    }

    @Test
    void eightExistingOrdersIsNotTenthYet() {
        assertFalse(isTenthOrder(8));
    }

    @Test
    void tenExistingOrdersIsNotAnotherTenth() {
        assertFalse(isTenthOrder(10));
    }

    @Test
    void generatedPasswordIsExactlyTenCharacters() {
        String pwd = generatePassword();
        assertEquals(10, pwd.length());
    }

    @Test
    void generatedPasswordIsNotNull() {
        String pwd = generatePassword();
        assertNotNull(pwd);
        assertFalse(pwd.isEmpty());
    }
}
