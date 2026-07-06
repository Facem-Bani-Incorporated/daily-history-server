package com.facem_bani_inc.daily_history_server.repository;

import com.facem_bani_inc.daily_history_server.entity.Friendship;
import com.facem_bani_inc.daily_history_server.model.enums.EFriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** Any relationship between two users, regardless of who sent the request. */
    @Query("""
           SELECT f FROM Friendship f
           WHERE (f.requester.id = :a AND f.addressee.id = :b)
              OR (f.requester.id = :b AND f.addressee.id = :a)
           """)
    Optional<Friendship> findBetween(@Param("a") Long a, @Param("b") Long b);

    /** Pending requests received by the given user (they can accept/decline). */
    @Query("""
           SELECT f FROM Friendship f
           JOIN FETCH f.requester
           WHERE f.addressee.id = :userId AND f.status = :status
           ORDER BY f.createdAt DESC
           """)
    List<Friendship> findIncoming(@Param("userId") Long userId, @Param("status") EFriendshipStatus status);

    /** Pending requests sent by the given user (still awaiting a response). */
    @Query("""
           SELECT f FROM Friendship f
           JOIN FETCH f.addressee
           WHERE f.requester.id = :userId AND f.status = :status
           ORDER BY f.createdAt DESC
           """)
    List<Friendship> findOutgoing(@Param("userId") Long userId, @Param("status") EFriendshipStatus status);

    /** All accepted friendships that involve the given user, on either side. */
    @Query("""
           SELECT f FROM Friendship f
           JOIN FETCH f.requester
           JOIN FETCH f.addressee
           WHERE f.status = :status
             AND (f.requester.id = :userId OR f.addressee.id = :userId)
           ORDER BY f.respondedAt DESC
           """)
    List<Friendship> findAcceptedForUser(@Param("userId") Long userId, @Param("status") EFriendshipStatus status);

    /** Cleanup helper for account deletion — removes every row touching the user. */
    @Modifying
    @Query("DELETE FROM Friendship f WHERE f.requester.id = :userId OR f.addressee.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
