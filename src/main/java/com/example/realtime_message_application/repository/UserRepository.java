package com.example.realtime_message_application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realtime_message_application.model.User;
import com.google.common.base.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> loadByUsername(String username);

    @Query("SELECT u FROM User u WHERE  LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%'))")
    List<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE  LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    User findByUsername(String username);

    @Query("SELECT u FROM User u " +
            "WHERE (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "OR (:keyword IS NULL or LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "OR (:keyword IS NULL or LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(u) > 0 FROM User u " +
            "WHERE u.username = :username AND u.id <> :id ")
        // use '<>' or '!=' if you want to exclude
    boolean existsByUsernameAndUserIdNot(@Param("username") String username, @Param("id") Long id);

    @Query("SELECT COUNT(u) > 0 FROM User u " +
            "WHERE u.username = :username ")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) > 0 FROM User u " +
            "WHERE u.phoneNumber = :phoneNumber AND u.id <> :id ")
    boolean existsByPhoneNumberUserIdNot(@Param("id") Long id, @Param("phoneNumber") String phoneNumber);

    @Query("SELECT COUNT(u) > 0 FROM User u " +
            "WHERE u.phoneNumber = :phoneNumber")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
