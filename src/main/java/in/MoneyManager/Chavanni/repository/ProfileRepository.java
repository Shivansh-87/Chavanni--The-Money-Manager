package in.MoneyManager.Chavanni.repository;

import in.MoneyManager.Chavanni.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    //This interface will automatically provide CRUD operations for ProfileEntity
    //You can add custom query methods here if needed

    //select * from tbl_profiles where email=?
    Optional<ProfileEntity> findByEmail(String email);

    Optional<ProfileEntity> findByActivationToken(String activationToken);
}
