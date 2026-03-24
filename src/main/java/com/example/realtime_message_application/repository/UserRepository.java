package com.example.realtime_message_application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realtime_message_application.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        @Query("SELECT u FROM User u WHERE u.username = :username")
        Optional<User> loadByUsername(@Param("username") String username);

        @Query("SELECT u FROM User u " +
                        "WHERE LOWER(u.phoneNo) LIKE LOWER(CONCAT('%', :phoneNo, '%'))")
        List<User> findByPhoneNo(String phoneNo);

        @Query("SELECT u FROM User u " +
                        "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
        User findByUsername(@Param("username") String username);

        @Query("SELECT u FROM User u " +
                        "WHERE (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "OR (:keyword IS NULL or LOWER(u.phoneNo) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "OR (:keyword IS NULL or LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        List<User> findByKeyword(@Param("keyword") String keyword);

        @Query("SELECT COUNT(u) > 0 FROM User u " +
                        "WHERE u.username = :username AND u.userId <> :userId ")
        // use '<>' or '!=' if you want to exclude
        boolean existsByUsernameAndUserIdNot(@Param("username") String username, @Param("userId") int userId);

        @Query("SELECT COUNT(u) > 0 FROM User u " +
                        "WHERE u.username = :username ")
        boolean existsByUsername(@Param("username") String username);

        @Query("SELECT COUNT(u) > 0 FROM User u " +
                        "WHERE u.phoneNo = :phoneNo AND u.userId <> :userId ")
        boolean existsByPhoneNoUserIdNot(@Param("userId") int userId, @Param("phoneNo") String phoneNo);

        @Query("SELECT COUNT(u) > 0 FROM User u " +
                        "WHERE u.phoneNo = :phoneNo")
        boolean existsByPhoneNo(@Param("phoneNo") String phoneNo);

        @Query("SELECT COUNT(u) > 0 FROM User u " +
                        "WHERE u.email = :email")
        boolean existsByEmail(@Param("email") String email);
}
