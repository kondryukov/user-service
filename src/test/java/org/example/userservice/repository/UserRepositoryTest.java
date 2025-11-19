package org.example.userservice.repository;

import org.example.userservice.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void saveAndFindById() {
        var u = new User();
        u.setName("name");
        u.setEmail("name@mail.ru");
        u.setAge(123);
        var saved = userRepository.save(u);

        assertThat(userRepository.existsUserByEmail("name@mail.ru")).isTrue();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void saveDuplicateEmail() {
        var user1 = new User();
        user1.setName("name");
        user1.setEmail("name@mail.ru");
        user1.setAge(123);
        userRepository.saveAndFlush(user1);


        var user2 = new User();
        user2.setName("name");
        user2.setEmail("name@mail.ru");
        user2.setAge(123);

        Assertions.assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user2));
    }
}