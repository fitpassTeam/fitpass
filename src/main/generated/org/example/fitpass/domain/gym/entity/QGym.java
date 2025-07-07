package org.example.fitpass.domain.gym.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGym is a Querydsl query type for Gym
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGym extends EntityPathBase<Gym> {

    private static final long serialVersionUID = -373098701L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGym gym = new QGym("gym");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    public final StringPath city = createString("city");

    public final TimePath<java.time.LocalTime> closeTime = createTime("closeTime", java.time.LocalTime.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath detailAddress = createString("detailAddress");

    public final StringPath district = createString("district");

    public final EnumPath<org.example.fitpass.domain.gym.enums.GymPostStatus> gymPostStatus = createEnum("gymPostStatus", org.example.fitpass.domain.gym.enums.GymPostStatus.class);

    public final EnumPath<org.example.fitpass.domain.gym.enums.GymStatus> gymStatus = createEnum("gymStatus", org.example.fitpass.domain.gym.enums.GymStatus.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage> images = this.<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage>createList("images", org.example.fitpass.common.Image.entity.Image.class, org.example.fitpass.common.Image.entity.QImage.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final StringPath number = createString("number");

    public final TimePath<java.time.LocalTime> openTime = createTime("openTime", java.time.LocalTime.class);

    public final StringPath summary = createString("summary");

    public final ListPath<org.example.fitpass.domain.trainer.entity.Trainer, org.example.fitpass.domain.trainer.entity.QTrainer> trainers = this.<org.example.fitpass.domain.trainer.entity.Trainer, org.example.fitpass.domain.trainer.entity.QTrainer>createList("trainers", org.example.fitpass.domain.trainer.entity.Trainer.class, org.example.fitpass.domain.trainer.entity.QTrainer.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final org.example.fitpass.domain.user.entity.QUser user;

    public QGym(String variable) {
        this(Gym.class, forVariable(variable), INITS);
    }

    public QGym(Path<? extends Gym> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGym(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGym(PathMetadata metadata, PathInits inits) {
        this(Gym.class, metadata, inits);
    }

    public QGym(Class<? extends Gym> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new org.example.fitpass.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

