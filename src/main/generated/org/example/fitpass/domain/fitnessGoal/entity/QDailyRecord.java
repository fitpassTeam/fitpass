package org.example.fitpass.domain.fitnessGoal.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDailyRecord is a Querydsl query type for DailyRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyRecord extends EntityPathBase<DailyRecord> {

    private static final long serialVersionUID = 853970402L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDailyRecord dailyRecord = new QDailyRecord("dailyRecord");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final QFitnessGoal fitnessGoal;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage> images = this.<org.example.fitpass.common.Image.entity.Image, org.example.fitpass.common.Image.entity.QImage>createList("images", org.example.fitpass.common.Image.entity.Image.class, org.example.fitpass.common.Image.entity.QImage.class, PathInits.DIRECT2);

    public final StringPath memo = createString("memo");

    public final DatePath<java.time.LocalDate> recordDate = createDate("recordDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDailyRecord(String variable) {
        this(DailyRecord.class, forVariable(variable), INITS);
    }

    public QDailyRecord(Path<? extends DailyRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDailyRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDailyRecord(PathMetadata metadata, PathInits inits) {
        this(DailyRecord.class, metadata, inits);
    }

    public QDailyRecord(Class<? extends DailyRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.fitnessGoal = inits.isInitialized("fitnessGoal") ? new QFitnessGoal(forProperty("fitnessGoal"), inits.get("fitnessGoal")) : null;
    }

}

