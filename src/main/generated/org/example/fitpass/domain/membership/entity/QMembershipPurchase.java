package org.example.fitpass.domain.membership.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMembershipPurchase is a Querydsl query type for MembershipPurchase
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMembershipPurchase extends EntityPathBase<MembershipPurchase> {

    private static final long serialVersionUID = 600549114L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMembershipPurchase membershipPurchase = new QMembershipPurchase("membershipPurchase");

    public final org.example.fitpass.common.QBaseEntity _super = new org.example.fitpass.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final org.example.fitpass.domain.gym.entity.QGym gym;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QMembership membership;

    public final DateTimePath<java.time.LocalDateTime> purchaseDate = createDateTime("purchaseDate", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> scheduledStartDate = createDateTime("scheduledStartDate", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final org.example.fitpass.domain.user.entity.QUser user;

    public QMembershipPurchase(String variable) {
        this(MembershipPurchase.class, forVariable(variable), INITS);
    }

    public QMembershipPurchase(Path<? extends MembershipPurchase> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMembershipPurchase(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMembershipPurchase(PathMetadata metadata, PathInits inits) {
        this(MembershipPurchase.class, metadata, inits);
    }

    public QMembershipPurchase(Class<? extends MembershipPurchase> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.gym = inits.isInitialized("gym") ? new org.example.fitpass.domain.gym.entity.QGym(forProperty("gym"), inits.get("gym")) : null;
        this.membership = inits.isInitialized("membership") ? new QMembership(forProperty("membership"), inits.get("membership")) : null;
        this.user = inits.isInitialized("user") ? new org.example.fitpass.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

