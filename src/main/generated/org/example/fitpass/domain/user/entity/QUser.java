package org.example.fitpass.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -856084093L;

    public static final QUser user = new QUser("user");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    public final StringPath address = createString("address");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final StringPath authProvider = createString("authProvider");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath email = createString("email");

    public final EnumPath<org.example.fitpass.domain.user.enums.Gender> gender = createEnum("gender", org.example.fitpass.domain.user.enums.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<org.example.fitpass.domain.likes.entity.Like, org.example.fitpass.domain.likes.entity.QLike> likes = this.<org.example.fitpass.domain.likes.entity.Like, org.example.fitpass.domain.likes.entity.QLike>createList("likes", org.example.fitpass.domain.likes.entity.Like.class, org.example.fitpass.domain.likes.entity.QLike.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final NumberPath<Integer> pointBalance = createNumber("pointBalance", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath userImage = createString("userImage");

    public final EnumPath<org.example.fitpass.domain.user.enums.UserRole> userRole = createEnum("userRole", org.example.fitpass.domain.user.enums.UserRole.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

