package org.example.fitpass.domain.trainer.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTrainer is a Querydsl query type for Trainer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTrainer extends EntityPathBase<Trainer> {

    private static final long serialVersionUID = -1687789581L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTrainer trainer = new QTrainer("trainer");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath experience = createString("experience");

    public final org.example.fitpass.domain.gym.entity.QGym gym;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage> images = this.<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage>createList("images", org.example.fitpass.common.Image.entity.Image.class, org.example.fitpass.common.Image.entity.QImage.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final EnumPath<org.example.fitpass.domain.trainer.enums.TrainerStatus> trainerStatus = createEnum("trainerStatus", org.example.fitpass.domain.trainer.enums.TrainerStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTrainer(String variable) {
        this(Trainer.class, forVariable(variable), INITS);
    }

    public QTrainer(Path<? extends Trainer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTrainer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTrainer(PathMetadata metadata, PathInits inits) {
        this(Trainer.class, metadata, inits);
    }

    public QTrainer(Class<? extends Trainer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gym = inits.isInitialized("gym") ? new org.example.fitpass.domain.gym.entity.QGym(forProperty("gym"), inits.get("gym")) : null;
    }

}

