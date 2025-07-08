package org.example.fitpass.domain.post.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.comment.repository.CommentRepository;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.repository.LikeRepository;
import org.example.fitpass.domain.post.dto.response.PostImageResponseDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final GymRepository gymRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    //게시물 생성
    @Transactional
    public PostResponseDto createPost(PostStatus postStatus, PostType postType,List<String> postImage , String title, String content, Long userId, Long gymId) {

        User user = userRepository.findByIdOrElseThrow(userId);
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        if (postType == PostType.NOTICE && user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOTICE_ONLY_OWNER);
        }
        if (!user.getId().equals(gym.getUser().getId())) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }

        Post post = Post.of(postStatus,postType,postImage,title,content,user,gym);
        postRepository.save(post);
        return PostResponseDto.of(
                post.getId(),
                post.getPostStatus(),
                post.getPostType(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getId(),
                post.getGym().getId(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                null,
                null,
            null,
            null
        );
    }

    //General 게시물 전체 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> findAllPostByGeneral(Pageable pageable, Long userId, Long gymId, PostType postType) {

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        Page<Post> posts = postRepository.findByGymIdAndPostType(gymId, postType, pageable);

        Set<Long> likedPostIds = (userId != null) // null 값이 들어와도 에러가 발생하지 않고 사용하기 위하여 Set 사용
            ? likeRepository.findTargetIdsByUserIdAndLikeType(userId, LikeType.POST)
            : Collections.emptySet();

        return posts.map(post -> {
            Long postId = post.getId();
            long likeCount = likeRepository.countByLikeTypeAndTargetId(LikeType.POST, postId);
            long commentCount = commentRepository.countByPostId(postId); // 댓글 수 조회
            boolean isLiked = likedPostIds.contains(postId); // 좋아요 여부 판단

            return PostResponseDto.from(post, likeCount, commentCount, isLiked);
        });
    }

    //Notice 게시물 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> findAllPostByNotice(Long userId, Long gymId, PostType postType) {

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        List<Post> posts = postRepository.findByGymIdAndPostTypeOrderByCreatedAtDesc(gymId, postType);

        Set<Long> likedPostIds = (userId != null) // null 값이 들어와도 에러가 발생하지 않고 사용하기 위하여 Set 사용
            ? likeRepository.findTargetIdsByUserIdAndLikeType(userId, LikeType.POST)
            : Collections.emptySet();

        return posts.stream()
            .map(post -> {
                Long postId = post.getId();
                long likeCount = likeRepository.countByLikeTypeAndTargetId(LikeType.POST, postId);
                long commentCount = commentRepository.countByPostId(postId); // 댓글 수 조회
                boolean isLiked = likedPostIds.contains(postId); // 좋아요 여부 판단

                return PostResponseDto.from(post, likeCount, commentCount, isLiked);
            })
            .toList(); // 또는 .collect(Collectors.toList())도 가능
    }

    //게시물 단건 조회
    @Transactional(readOnly = true)
    public PostImageResponseDto findPostById(User user, Long gymId, Long postId) {
        User finduser = userRepository.findByIdOrElseThrow(user.getId());
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Post post = postRepository.findByIdOrElseThrow(postId);
        return PostImageResponseDto.from(
                post.getId(),
                post.getPostImages().stream().map(Image::getUrl).toList(),
                post.getPostStatus(),
                post.getPostType(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getId(),
                post.getGym().getId(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    //게시물 수정
    @Transactional
    public PostResponseDto updatePost(Long postId, PostStatus status, PostType postType, String title, String content, Long userId, Long gymId, List<String> postImage) {
        Post post = postRepository.findByIdOrElseThrow(postId);
        User findUser = userRepository.findByIdOrElseThrow(userId);
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        if (!findUser.getId().equals(post.getUser().getId())) {
            throw new BaseException(ExceptionCode.POST_NOT_AUTHOR);
        }
        if (postType == PostType.NOTICE && findUser.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOTICE_ONLY_OWNER);
        }
        post.getPostImages().clear();
        post.update(status,postType,title,content,postImage);
        return PostResponseDto.of(
                postId,
                post.getPostStatus(),
                post.getPostType(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getId(),
                post.getGym().getId(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPostImages()
                    .stream()
                    .map(Image::getUrl)
                    .toList(),
                null,
            null,
            null
        );
    }

    public void deletePost(Long gymId, Long postId, Long userId) {
        User findUser = userRepository.findByIdOrElseThrow(userId);
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Post post = postRepository.findByIdOrElseThrow(postId);

        if (!findUser.getId().equals(post.getUser().getId())) {
            throw new BaseException(ExceptionCode.POST_NOT_AUTHOR);
        }

        postRepository.delete(post);
    }
}
