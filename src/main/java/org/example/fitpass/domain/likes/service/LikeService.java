package org.example.fitpass.domain.likes.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.entity.Like;
import org.example.fitpass.domain.likes.repository.LikeRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public void postGymLike(Long userId, Long gymId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Optional<Like> likeOptional = likeRepository.findByUserAndTargetId(user, gymId);
        if(likeOptional.isEmpty()){
            Like like = Like.of(user, LikeType.GYM, gymId);
            likeRepository.save(like);
        }else {
            likeRepository.delete(likeOptional.get());
        }
    }

    @Transactional
    public void postLike(Long userId, Long postId){
        User user = userRepository.findByIdOrElseThrow(userId);
        Optional<Like> likeOptional = likeRepository.findByUserAndTargetId(user, postId);
        if(likeOptional.isEmpty()){
            Like like = Like.of(user, LikeType.GYM, postId);
            likeRepository.save(like);
        }else {
            likeRepository.delete(likeOptional.get());
        }
    }
}
