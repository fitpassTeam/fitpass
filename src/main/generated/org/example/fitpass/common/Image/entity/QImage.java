package org.example.fitpass.common.Image.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QImage is a Querydsl query type for Image
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QImage extends EntityPathBase<Image> {

    private static final long serialVersionUID = 1386875084L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QImage image = new QImage("image");

    public final org.example.fitpass.domain.fitnessGoal.entity.QDailyRecord dailyRecord;

    public final org.example.fitpass.domain.gym.entity.QGym gym;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final org.example.fitpass.domain.post.entity.QPost post;

    public final org.example.fitpass.domain.trainer.entity.QTrainer trainer;

    public final StringPath url = createString("url");

    public QImage(String variable) {
        this(Image.class, forVariable(variable), INITS);
    }

    public QImage(Path<? extends Image> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QImage(PathMetadata metadata, PathInits inits) {
        this(Image.class, metadata, inits);
    }

    public QImage(Class<? extends Image> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.dailyRecord = inits.isInitialized("dailyRecord") ? new org.example.fitpass.domain.fitnessGoal.entity.QDailyRecord(forProperty("dailyRecord"), inits.get("dailyRecord")) : null;
        this.gym = inits.isInitialized("gym") ? new org.example.fitpass.domain.gym.entity.QGym(forProperty("gym"), inits.get("gym")) : null;
        this.post = inits.isInitialized("post") ? new org.example.fitpass.domain.post.entity.QPost(forProperty("post"), inits.get("post")) : null;
        this.trainer = inits.isInitialized("trainer") ? new org.example.fitpass.domain.trainer.entity.QTrainer(forProperty("trainer"), inits.get("trainer")) : null;
    }

}

