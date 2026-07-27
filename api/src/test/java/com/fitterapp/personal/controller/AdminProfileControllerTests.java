package com.fitterapp.personal.controller;
import static org.assertj.core.api.Assertions.*; import static org.mockito.ArgumentMatchers.any; import static org.mockito.Mockito.*;
import java.time.Instant; import java.util.*; import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.*; import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension; import org.springframework.security.oauth2.jwt.Jwt;
import com.fitterapp.personal.dto.review.*; import com.fitterapp.personal.service.review.*;
import com.fitterapp.personal.mapper.ProfileMapper; import com.fitterapp.personal.service.query.ListProfilesForReviewService;
@ExtendWith(MockitoExtension.class) class AdminProfileControllerTests {
 @Mock ReviewProfileService review; @Mock ListProfilesForReviewService pending;
 @Test void rejectsUsingAdminJwtSubject(){UUID admin=UUID.randomUUID(),profile=UUID.randomUUID(),revision=UUID.randomUUID();when(review.reject(any())).thenReturn(new ReviewProfileResult(profile,revision));var response=new AdminProfileController(review,pending,new ProfileMapper()).reject(jwt(admin),profile,new RejectProfileRequestDto("CREF inválido"));assertThat(response.getStatusCode().value()).isEqualTo(200);verify(review).reject(new RejectProfileCommand(admin,profile,"CREF inválido"));}
 private Jwt jwt(UUID id){return new Jwt("token",Instant.now(),Instant.now().plusSeconds(60),Map.of("alg","none"),Map.of("sub",id.toString()));}
}
