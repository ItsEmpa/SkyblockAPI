package tech.thatgravyboat.skyblockapi.api.remote.hypixel.requirements

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.*
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonType
import tech.thatgravyboat.skyblockapi.api.area.isle.kuudra.KuudraTier
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerType
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.HypixelSkillAPI
import tech.thatgravyboat.skyblockapi.generated.DispatchHelper
import tech.thatgravyboat.skyblockapi.utils.codecs.EnumCodec
import tech.thatgravyboat.skyblockapi.utils.extentions.parseRomanNumeral
import tech.thatgravyboat.skyblockapi.utils.extentions.toRomanNumeral
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toTimeUnit

@GenerateDispatchCodec(ItemRequirement::class)
enum class ItemRequirementType(
    override val type: KClass<out ItemRequirement>,
    id: String? = null,
) : DispatchHelper<ItemRequirement> {
    SLAYER(SlayerItemRequirement::class),
    SKILL(SkillItemRequirement::class),
    DUNGEON_FLOOR_COMPLETION(DungeonFloorCompletionItemRequirement::class, id = "DUNGEON_TIER"),
    HOTM(HotmItemRequirement::class, id = "HEART_OF_THE_MOUNTAIN"),

    DUNGEON_SKILL(DungeonSkillItemRequirement::class),
    TARGET_PRACTICE(TargetPracticeItemRequirement::class),
    ONE_OF(OneOfItemRequirement::class),
    KUUDRA_COMPLETION(KuudraCompletionItemRequirement::class),
    CHOCOLATE_FACTORY(ChocolateFactoryItemRequirement::class),
    GARDEN_LEVEL(GardenLevelItemRequirement::class),
    PROFILE_AGE(ProfileAgeItemRequirement::class),
    PROFILE_TYPE(ProfileTypeItemRequirement::class),
    MELODY_HAIR(MelodyHairItemRequirement::class),
    //TROPHY_FISHING,
    //CRIMSON_ISLE_REPUTATION,
    //COLLECTION,
    //EASTER_RABBIT,
    UNKNOWN(UnknownItemRequirement::class),
    ;

    override val id: String = id ?: name

    companion object {
        fun getType(id: String) = entries.find { it.id.equals(id, true) } ?: UNKNOWN
    }
}

sealed class ItemRequirement(val type: ItemRequirementType) {
    open fun meetsRequirement(): Boolean = false
}

@GenerateCodec
data class SlayerItemRequirement(
    @NamedCodec("slayer_type_api_name")
    @FieldName("slayer_boss_type")
    val slayer: SlayerType,
    val level: Int,
) : ItemRequirement(ItemRequirementType.SLAYER) {
    companion object {
        @IncludedCodec(named = "slayer_type_api_name")
        val slayerTypeCodec = EnumCodec.of(SlayerType.entries.toTypedArray(), SlayerType::apiName)
    }
}

@GenerateCodec
data class SkillItemRequirement(
    val skill: HypixelSkillAPI.Skill,
    val level: Int,
) : ItemRequirement(ItemRequirementType.SKILL)

@GenerateCodec
data class DungeonFloorCompletionItemRequirement(
    @FieldName("dungeon_type") val dungeonType: DungeonType,
    val tier: Int,
) : ItemRequirement(ItemRequirementType.DUNGEON_FLOOR_COMPLETION)

@GenerateCodec
data class HotmItemRequirement(
    val tier: Int,
) : ItemRequirement(ItemRequirementType.HOTM)

@GenerateCodec
data class DungeonSkillItemRequirement(
    @FieldName("dungeon_type") val dungeonType: DungeonType,
    val level: Int
) : ItemRequirement(ItemRequirementType.DUNGEON_SKILL)

data class TargetPracticeItemRequirement(
    val mode: Int
) : ItemRequirement(ItemRequirementType.TARGET_PRACTICE) {
    companion object {
        @IncludedCodec
        val CODEC: Codec<TargetPracticeItemRequirement> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.STRING.xmap(String::parseRomanNumeral) { it.toRomanNumeral() }.fieldOf("mode")
                    .forGetter(TargetPracticeItemRequirement::mode)
            ).apply(builder) { mode ->
                TargetPracticeItemRequirement(mode)
            }
        }
    }
}

@GenerateCodec
data class OneOfItemRequirement(
    val requirements: List<ItemRequirement>,
) : ItemRequirement(ItemRequirementType.ONE_OF) {
    override fun meetsRequirement(): Boolean = requirements.any(ItemRequirement::meetsRequirement)
}

@GenerateCodec
data class KuudraCompletionItemRequirement(
    @FieldName("kuudra_tier") val tier: KuudraTier
) : ItemRequirement(ItemRequirementType.KUUDRA_COMPLETION)

@GenerateCodec
data class ChocolateFactoryItemRequirement(
    val level: Int
) : ItemRequirement(ItemRequirementType.CHOCOLATE_FACTORY)

@GenerateCodec
data class GardenLevelItemRequirement(
    val level: Int
) : ItemRequirement(ItemRequirementType.GARDEN_LEVEL)

@GenerateCodec
data class ProfileAgeItemRequirement(
    @FieldName("minimum_age_unit") val unit: DurationUnit,
    @FieldName("minimum_age") val minimumAge: Long,
) : ItemRequirement(ItemRequirementType.PROFILE_AGE) {
    val duration: Duration = unit.toTimeUnit().toMillis(minimumAge).milliseconds
}

@GenerateCodec
data class ProfileTypeItemRequirement(
    @FieldName("profile_type")
    @NamedCodec("profile_type_api_name")
    val profileType: ProfileType,
) : ItemRequirement(ItemRequirementType.PROFILE_TYPE) {
    override fun meetsRequirement(): Boolean = ProfileAPI.profileType == profileType
    companion object {
        @IncludedCodec(named = "profile_type_api_name")
        val profileTypeApiCodec = EnumCodec.of(ProfileType.entries.toTypedArray(), ProfileType::apiName)
    }
}

object MelodyHairItemRequirement : ItemRequirement(ItemRequirementType.MELODY_HAIR) {
    @IncludedCodec
    val CODEC: Codec<MelodyHairItemRequirement> = Codec.unit(this)
}

object UnknownItemRequirement : ItemRequirement(ItemRequirementType.UNKNOWN) {
    @IncludedCodec
    val CODEC: Codec<UnknownItemRequirement> = Codec.unit(this)
    override fun meetsRequirement(): Boolean = true
}
