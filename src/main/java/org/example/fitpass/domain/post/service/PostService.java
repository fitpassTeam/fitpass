package org.example.fitpass.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.post.dto.response.PostImageResponseDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final GymRepository gymRepository;
    private final S3Service s3Service;
    //게시물 생성
    @Transactional
    public PostResponseDto createPost(PostStatus postStatus, PostType postType,List<String> postImage , String title, String content, Long userId, Long gymId) {

        User user = userRepository.findByIdOrElseThrow(userId);
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        if (postType == PostType.NOTICE && user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOTICE_ONLY_OWNER);
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
                post.getUpdatedAt()
        );
    }

    //General 게시물 전체 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> findAllPostByGeneral(Pageable pageable, User user, Long gymId, PostType postType) {

        User finduser = userRepository.findByIdOrElseThrow(user.getId());

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        Page<Post> posts = postRepository.findByGymIdAndPostType(gymId, postType, pageable);

        return posts.map(PostResponseDto::from);
    }

    //Notice 게시물 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> findAllPostByNotice(User user, Long gymId, PostType postType) {

        User finduser = userRepository.findByIdOrElseThrow(user.getId());

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        List<Post> posts = postRepository.findByGymIdAndPostTypeOrderByCreatedAtDesc(gymId, postType);

        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
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
    //사진 수정
    @Transactional
    public List<String> updatePhoto(List<MultipartFile> files, Long postId, Long userId) {
        Post post = postRepository.findByIdOrElseThrow(postId);
        post.isOwner(userId);
        for (Image image : post.getPostImages()) {
            s3Service.deleteFileFromS3(image.getUrl());
        }
        List<String> imageUrls = s3Service.uploadFiles(files);
        post.updatePhoto(imageUrls, post);
        postRepository.save(post);
        return imageUrls;
    }

    //게시물 수정
    @Transactional
    public PostResponseDto updatePost(Long postId, PostStatus status, PostType postType, String title, String content, Long userId, Long gymId) {

        Post post = postRepository.findByIdOrElseThrow(postId);
        User findUser = userRepository.findByIdOrElseThrow(userId);
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        if (!findUser.getId().equals(post.getUser().getId())) {
            throw new BaseException(ExceptionCode.POST_NOT_AUTHOR);
        }
        if (postType == PostType.NOTICE && findUser.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOTICE_ONLY_OWNER);
        }
        post.update(status,postType,title,content);
        return PostResponseDto.of(
                postId,
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
}
