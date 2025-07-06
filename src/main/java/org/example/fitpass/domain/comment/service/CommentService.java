package org.example.fitpass.domain.comment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.comment.dto.request.CommentRequestDto;
import org.example.fitpass.domain.comment.dto.response.CommentResponseDto;
import org.example.fitpass.domain.comment.entity.Comment;
import org.example.fitpass.domain.comment.repository.CommentRepository;
import org.example.fitpass.domain.post.entity.Post;
import org.example.fitpass.domain.post.repository.PostRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(Long postId, Long id, String content, Long parentId) {
        Post post = postRepository.findByIdOrElseThrow(postId);
        User user = userRepository.findByIdOrElseThrow(id);

        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findByIdOrElseThrow(parentId);
        }

        Comment comment = Comment.of(post, user, content, parent);
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentIsNull(postId);
        return comments.stream()
            .map(CommentResponseDto::from)
            .toList();
    }

    @Transactional
    public void updateComment(Long commentId, String content, Long id, Long postId) {
        Post post = postRepository.findByIdOrElseThrow(postId);
        Comment comment = commentRepository.findByIdOrElseThrow(commentId);
        User user = userRepository.findByIdOrElseThrow(id);

        if(!comment.getUser().equals(user)){
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        comment.update(content);
    }

    public void deleteComment(Long commentId, Long id, Long postId) {
        Post post = postRepository.findByIdOrElseThrow(postId);
        Comment comment = commentRepository.findByIdOrElseThrow(commentId);
        User user = userRepository.findByIdOrElseThrow(id);

        if (!comment.getUser().getId().equals(id) && !comment.getPost().getUser().getId().equals(id)) {
            throw new BaseException(ExceptionCode.NOT_HAS_AUTHORITY);
        }
        commentRepository.delete(comment);
    }

}
