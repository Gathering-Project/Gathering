package nbc_final.gathering.domain.user.repository;

import nbc_final.gathering.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    //     @Query("SELECT u FROM User u WHERE u.id = :userId AND u.isDeleted = true")
    Optional<User> findByEmailAndIsDeletedTrue(String email); // 탈퇴한 계정인지 확인
}
