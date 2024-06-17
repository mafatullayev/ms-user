package com.example.msuser

import com.example.msuser.dao.entity.UserEntity
import com.example.msuser.dao.repository.UserRepository
import com.example.msuser.exception.NotFoundException
import com.example.msuser.exception.WrongCredentialsException
import com.example.msuser.model.request.AuthRequest
import com.example.msuser.model.request.CreateUserRequest
import com.example.msuser.service.abstraction.UserService
import com.example.msuser.service.concrete.UserServiceHandler
import com.example.msuser.util.SecurityUtil
import io.github.benas.randombeans.EnhancedRandomBuilder
import io.github.benas.randombeans.api.EnhancedRandom
import org.hibernate.annotations.NotFound
import spock.lang.Specification

import static com.example.msuser.mapper.UserMapper.USER_MAPPER

class UserServiceHandlerTest extends Specification {
    EnhancedRandom random = EnhancedRandomBuilder.aNewEnhancedRandom()
    UserService userService
    UserRepository userRepository
    SecurityUtil securityUtil

    def setup() {
        userRepository = Mock()
        securityUtil = Mock()
        userService = new UserServiceHandler(userRepository, securityUtil)
    }

    def "TestSignUp"() {
        given:
        def request = random.nextObject(CreateUserRequest)
        def entity = USER_MAPPER.buildUserEntity(request)

        when:
        userService.signUp(request)

        then:
        1 * request.setPassword(request)
        1 * userRepository.save(entity)
    }

    def "TestSignIn"() {
        given:
        def request = random.nextObject(AuthRequest)

        when:
        userService.getUser(request)

        then:
        1 * userRepository.findByMail(request) >> Optional.of(request)
        WrongCredentialsException ex = thrown()
        ex.message == "User not match with given credentials"
    }

    def "TestGetUser success"() {
        given:
        def id = random.nextLong()
        def entity = random.nextObject(UserEntity)
        def expected = USER_MAPPER.buildUserResponse(entity)

        when:
        def response = userService.getUser(id)

        then:
        1 * userRepository.findById(id) >> Optional.of(entity)
        expected == response
    }

    def "TestGetUser error user not found"() {
        given:
        def id = random.nextLong()

        when:
        userService.getUser(id)

        then:
        1 * userRepository.findById(id) >> Optional.empty()
        NotFoundException ex = thrown()
        ex.message == "User not found!"
    }

}