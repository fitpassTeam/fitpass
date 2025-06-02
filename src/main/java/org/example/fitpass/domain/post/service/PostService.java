//package org.example.fitpass.domain.post.service;
//
//import lombok.RequiredArgsConstructor;
//import org.example.fitpass.domain.gym.entity.Gym;
//import org.example.fitpass.domain.gym.repository.GymRepository;
//import org.example.fitpass.domain.post.dto.request.PostCreateRequestDto;
//import org.example.fitpass.domain.post.dto.request.PostUpdateRequestDto;
//import org.example.fitpass.domain.post.dto.response.PostResponseDto;
//import org.example.fitpass.domain.post.entity.Post;
//import org.example.fitpass.domain.post.enums.PostType;
//import org.example.fitpass.domain.post.repository.PostRepository;
//import org.example.fitpass.domain.user.UserRole;
//import org.example.fitpass.domain.user.entity.User;
//import org.example.fitpass.domain.user.repository.UserRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class PostService {
//
//    private final UserRepository userRepository;
//    private final PostRepository postRepository;
//    private final GymRepository gymRepository;
//
//    //게시물 생성
//    @Transactional
//    public PostResponseDto createPost(PostCreateRequestDto postRequestDto, User user, Long gymId){
//
//        User finduser = userRepository.findByIdOrElseThrow(user.getId());
//
//        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
//
//        if(postRequestDto.getPostType() == PostType.NOTICE && finduser.getUserRole() != UserRole.OWNER){
//            throw new IllegalArgumentException("공지사항은 관리자만 작성할 수 있습니다.");
//        }
//
//        Post post = new Post(
//                postRequestDto.getStatus(),
//                postRequestDto.getPostType(),
//                postRequestDto.getTitle(),
//                postRequestDto.getContent(),
//                postRequestDto.getPostImage(),
//                finduser,
//                gym
//        );
//
//        Post createPost = postRepository.save(post);
//
//        return PostResponseDto.from(createPost);
//    }
//
//    //게시물 전체 조회
//    @Transactional(readOnly = true)
//    public List<PostResponseDto> findAllPost (User user, Long gymId){
//
//        User finduser = userRepository.findByIdOrElseThrow(user.getId());
//
//        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
//
//        List<Post> posts = postRepository.findAll();
//
//        return posts.stream().map(PostResponseDto::from).toList();
//    }
//
//    //게시물 단건 조회
//    @Transactional(readOnly = true)
//    public PostResponseDto findPostById (User user, Long gymId, Long postId){
//
//        User finduser = userRepository.findByIdOrElseThrow(user.getId());
//
//        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
//
//        Post post = postRepository.findByIdOrElseThrow(postId);
//
//        return PostResponseDto.from(post);
//    }
//
//    //게시물 수정
//    @Transactional
//    public PostResponseDto updatePost (PostUpdateRequestDto requestDto, User user, Long gymId, Long postId){
//
//        Post post = postRepository.findByIdOrElseThrow(postId);
//
//        User finduser = userRepository.findByIdOrElseThrow(user.getId());
//
//        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
//
//        if(!finduser.equals(post.getUser().getId())){
//            throw new IllegalArgumentException("게시물 작성자만 수정이 가능 합니다");
//        }
//
//        post.update(
//          requestDto.getStatus(),
//          requestDto.getPostType(),
//          requestDto.getTitle(),
//          requestDto.getContent(),
//          requestDto.getPostImage()
//        );
//
//        if(requestDto.getPostType() == PostType.NOTICE && finduser.getUserRole() != UserRole.OWNER){
//            throw new IllegalArgumentException("공지사항은 관리자만 작성할 수 있습니다.");
//        }
//
//        Post updatePost = postRepository.save(post);
//
//        return PostResponseDto.from(updatePost);
//    }
//}
