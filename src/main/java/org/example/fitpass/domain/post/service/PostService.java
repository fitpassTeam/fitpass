package org.example.fitpass.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
import org.example.fitpass.domain.post.dto.response.PostResponseDto;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.enums.PostType;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;
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

    //게시물 생성
    @Transactional
    public PostResponseDto createPost(PostCreateRequestDto postRequestDto, User user, Long gymId) {

        User finduser = userRepository.findByIdOrElseThrow(user.getId());

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        if (postRequestDto.getPostType() == PostType.NOTICE && finduser.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOTICE_ONLY_OWNER);
        }

        Post post = new Post(
                postRequestDto.getStatus(),
                postRequestDto.getPostType(),
                postRequestDto.getTitle(),
                postRequestDto.getContent(),
                postRequestDto.getPostImage(),
                finduser,
                gym
        );

        Post createPost = postRepository.save(post);

        return PostResponseDto.from(createPost);
    }

    //게시물 전체 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> findAllPost(Pageable pageable, User user, Long gymId) {

        User finduser = userRepository.findByIdOrElseThrow(user.getId());

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        Page<Post> posts = postRepository.findAll(pageable);

        return posts.map(PostResponseDto::from);
    }

    //게시물 단건 조회
    @Transactional(readOnly = true)
    public PostResponseDto findPostById(User user, Long gymId, Long postId) {

        User finduser = userRepository.findByIdOrElseThrow(user.getId());

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        Post post = postRepository.findByIdOrElseThrow(postId);

        return PostResponseDto.from(post);
    }

    //게시물 수정
    @Transactional
    public PostResponseDto updatePost(PostUpdateRequestDto requestDto, User user, Long gymId, Long postId) {

        Post post = postRepository.findByIdOrElseThrow(postId);

        User findUser = userRepository.findByIdOrElseThrow(user.getId());

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        if (!findUser.getId().equals(post.getUser().getId())) {
            throw new BaseException(ExceptionCode.POST_NOT_AUTHOR);
        }

        post.update(
                requestDto.getStatus(),
                requestDto.getPostType(),
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getPostImage()
        );

        if (requestDto.getPostType() == PostType.NOTICE && findUser.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOTICE_ONLY_OWNER);
        }

        Post updatePost = postRepository.save(post);

        return PostResponseDto.from(updatePost);
    }
}
