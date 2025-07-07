package org.example.fitpass.domain.fitnessGoal.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWeightRecord is a Querydsl query type for WeightRecord
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWeightRecord extends EntityPathBase<WeightRecord> {

    private static final long serialVersionUID = 715232721L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWeightRecord weightRecord = new QWeightRecord("weightRecord");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final QFitnessGoal fitnessGoal;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath memo = createString("memo");

    public final DatePath<java.time.LocalDate> recordDate = createDate("recordDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Double> weight = createNumber("weight", Double.class);

    public QWeightRecord(String variable) {
        this(WeightRecord.class, forVariable(variable), INITS);
    }

    public QWeightRecord(Path<? extends WeightRecord> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWeightRecord(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWeightRecord(PathMetadata metadata, PathInits inits) {
        this(WeightRecord.class, metadata, inits);
    }

    public QWeightRecord(Class<? extends WeightRecord> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.fitnessGoal = inits.isInitialized("fitnessGoal") ? new QFitnessGoal(forProperty("fitnessGoal"), inits.get("fitnessGoal")) : null;
    }

}

