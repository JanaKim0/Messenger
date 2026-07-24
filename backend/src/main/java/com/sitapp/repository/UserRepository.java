package com.sitapp.repository;

import com.sitapp.domain.User;
import com.sitapp.domain.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    /**
     * Free-text search over username, first name and last name. Only approved
     * accounts are returned, and the caller ({@code excludedId}) is excluded.
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.status = com.sitapp.domain.UserStatus.APPROVED
              AND u.id <> :excludedId
              AND (
                    LOWER(u.username)  LIKE LOWER(CONCAT('%', :term, '%'))
                 OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :term, '%'))
                 OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :term, '%'))
              )
            ORDER BY u.firstName ASC, u.lastName ASC
            """)
    List<User> searchApproved(@Param("term") String term, @Param("excludedId") Long excludedId);
}
