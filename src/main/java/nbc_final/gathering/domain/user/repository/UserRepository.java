package nbc_final.gathering.domain.user.repository;

import nbc_final.gathering.domain.user.entity.User;
import nbc_final.gathering.domain.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

     boolean existsByEmail(String email);
     Optional<User> findByEmail(String email);

//     @Query("SELECT u FROM User u WHERE u.id = :userId AND u.isDeleted = true")
     Optional<User> findByEmailAndIsDeletedTrue(String email); // 탈퇴한 계정인지 확인


     boolean existsByMemberIdAndUserIdAndUserRole(Long userId, Long eventId, UserRole userRole); //CommentService에서 user권한 확인
}
