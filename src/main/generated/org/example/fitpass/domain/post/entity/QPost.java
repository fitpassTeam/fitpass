package org.example.fitpass.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = 886502317L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPost post = new QPost("post");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    public final ListPath<org.example.fitpass.domain.comment.entity.Comment, org.example.fitpass.domain.comment.entity.QComment> comments = this.<org.example.fitpass.domain.comment.entity.Comment, org.example.fitpass.domain.comment.entity.QComment>createList("comments", org.example.fitpass.domain.comment.entity.Comment.class, org.example.fitpass.domain.comment.entity.QComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final org.example.fitpass.domain.gym.entity.QGym gym;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage> postImages = this.<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage>createList("postImages", org.example.fitpass.common.Image.entity.Image.class, org.example.fitpass.common.Image.entity.QImage.class, PathInits.DIRECT2);

    public final EnumPath<org.example.fitpass.domain.post.enums.PostStatus> postStatus = createEnum("postStatus", org.example.fitpass.domain.post.enums.PostStatus.class);

    public final EnumPath<org.example.fitpass.domain.post.enums.PostType> postType = createEnum("postType", org.example.fitpass.domain.post.enums.PostType.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final org.example.fitpass.domain.user.entity.QUser user;

    public QPost(String variable) {
        this(Post.class, forVariable(variable), INITS);
    }

    public QPost(Path<? extends Post> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPost(PathMetadata metadata, PathInits inits) {
        this(Post.class, metadata, inits);
    }

    public QPost(Class<? extends Post> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gym = inits.isInitialized("gym") ? new org.example.fitpass.domain.gym.entity.QGym(forProperty("gym"), inits.get("gym")) : null;
        this.user = inits.isInitialized("user") ? new org.example.fitpass.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

