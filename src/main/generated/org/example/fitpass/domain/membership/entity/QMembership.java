package org.example.fitpass.domain.membership.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMembership is a Querydsl query type for Membership
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMembership extends EntityPathBase<Membership> {

    private static final long serialVersionUID = 764159705L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMembership membership = new QMembership("membership");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Integer> durationInDays = createNumber("durationInDays", Integer.class);

    public final org.example.fitpass.domain.gym.entity.QGym gym;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMembership(String variable) {
        this(Membership.class, forVariable(variable), INITS);
    }

    public QMembership(Path<? extends Membership> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMembership(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMembership(PathMetadata metadata, PathInits inits) {
        this(Membership.class, metadata, inits);
    }

    public QMembership(Class<? extends Membership> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gym = inits.isInitialized("gym") ? new org.example.fitpass.domain.gym.entity.QGym(forProperty("gym"), inits.get("gym")) : null;
    }

}

