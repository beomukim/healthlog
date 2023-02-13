package com.health.healthlog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import com.health.healthlog.domain.Article;
import com.health.healthlog.domain.UserAccount;
import com.health.healthlog.dto.ArticleDto;
import com.health.healthlog.dto.ArticleWithTrainingsDto;
import com.health.healthlog.dto.UserAccountDto;
import com.health.healthlog.repository.ArticleRepository;
import com.health.healthlog.repository.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("비즈니스 로직 -게시글")
@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {
    @InjectMocks
    private ArticleService sut;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @DisplayName("게시글을 검색하면, 게시글 리스트를 반환")
    @Test
    void givenSearchParameters_whenSearchingArticles_thenReturnsArticleList() {
        // Given
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());

        // When
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable);

        // Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("게시글을 조회하면, 게시글을 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
        // Given
        Article result = createArticle();
        given(articleRepository.findById(any())).willReturn(Optional.of(result));

        // When
        ArticleDto article = sut.searchArticle(1L);

        // Then
        ArticleDto articleDto = ArticleDto.from(result);
        assertThat(article).isNotNull().isEqualTo(articleDto);
    }

    @DisplayName("게시글 정보를 입력하면, 게시글을 생성한다")
    @Test
    void givenArticleInfo_whenSavingArticle_thenSavesArticle() {
        // Given
        given(articleRepository.save(any(Article.class))).willReturn(null);

        // When
        sut.saveArticle(createArticleDto("content"));

        // Then
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("게시글의 ID와 수정 정보를 입력하면, 게시글을 수정한다")
    @Test
    void givenModifiedArticleInfo_whenUpdatingArticle_thenUpdatesArticle() {
        // Given
        Article article = createArticle(); // createUserAccount
        ArticleDto dto = createArticleDto( "새 내용"); // createUserAccountDto
        given(articleRepository.getReferenceById(dto.id())).willReturn(article);
        given(userAccountRepository.getReferenceById(dto.userAccountDto().userId())).willReturn(dto.userAccountDto().toEntity());

        // When
        sut.updateArticle(dto.id(), dto);

        // Then
        assertThat(article)
                .hasFieldOrPropertyWithValue("content", dto.content());
        then(articleRepository).should().getReferenceById(dto.id());
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().userId());
    }

    @DisplayName("게시글의 ID를 입력하면, 게시글을 삭제한다")
    @Test
    void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
        // Given
        Long articleId = 1L;
        String userId = "beomu";
        willDoNothing().given(articleRepository).deleteByIdAndUserAccount_UserId(articleId, userId);

        // When
        sut.deleteArticle(1L, userId);

        // Then
        then(articleRepository).should().deleteByIdAndUserAccount_UserId(articleId, userId);
    }

    @DisplayName("게시글 ID로 조회하면, 해당 게시글 트레이닝이 포함된 게시글 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticleWithTrainings_thenReturnsArticleWithTrainings() {
        // Given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        // When
        ArticleWithTrainingsDto dto = sut.getArticleWithTrainings(articleId);

        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("content", article.getContent());
        then(articleRepository).should().findById(articleId);
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "beomu",
                "password",
                "beomu@email.com",
                "beomu",
                "This is memo"
        );
    }

    private Article createArticle() {
        Article article = Article.of(createUserAccount(), "content");
        ReflectionTestUtils.setField(article, "id", 1L);
        return article;
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "beomu",
                "password",
                "beomu@email.com",
                "beomu",
                "This is memo",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private ArticleDto createArticleDto(String content) {
        return ArticleDto.of(
                1L,
                createUserAccountDto(),
                content,
                LocalDateTime.now()
        );
    }
}
