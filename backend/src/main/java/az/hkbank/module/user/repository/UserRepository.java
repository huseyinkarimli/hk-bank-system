package az.hkbank.module.user.repository;

import az.hkbank.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides database access methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address.
     *
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by phone number.
     *
     * @param phoneNumber the phone number
     * @return Optional containing the user if found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email address
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given phone number.
     *
     * @param phoneNumber the phone number
     * @return true if user exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Finds all users that are not soft-deleted.
     *
     * @return list of active users
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAll();
}
