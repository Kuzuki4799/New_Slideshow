package com.acatapps.videomaker.slide_show_transition

import com.acatapps.videomaker.application.VideoMakerApplication
import com.acatapps.videomaker.slide_show_transition.transition.*
import com.acatapps.videomaker.utils.RawResourceReader

object  GSTransitionUtils {

    enum class TransitionType {
        NONE,
        ANGULAR,
        BOUNCE,
        BOW_TIE_HORIZONTAL,
        BOW_TIE_VERTICAL,
        BUTTERFLY_WAVE,
        CANNABIS_LEAF,
        CIRCLE_CROP,
        CIRCLE,
        CIRCLE_OPEN,
        COLOR_PHASE,
        COLOR_DISTANCE,
        CRAZY_PARAMETRIC,
        CROSS_HATCH,
        CROSS_WARP,
        CROSS_ZOOM,
        CUBE,
        DIRECTION_WIPE,
        DIRECTION_WARP,
        DOOM_SCREEN,
        DOOR_WAY,
        DREAMY,
        FADE_GRAY_SCALE,
        FLY_EYE,
        GLITCH,
        GRID_FLIP,
        IN_HEART,
        INVERTED_PAGE_CURL,
        KALE_IDO_SCOPE,
        LINEAR_BLUR,
        MORPH,
        MOSAIC,
        MULTIPLY_BLEND,
        PER_LIN,
        PIN_WHEEL,
        PIXEL_IZE,
        POLAR_FUN,
        POLKA_DOTS,
        RADIAL,
        RANDOM_SQUARE,
        RIPPLE,
        ROTATE_SCALE_FADE,
        SIMPLE_ZOOM,
        SQUARE_WIRE,
        SQUEEZE,
        SWAP,
        SWIRL,
        UNDULATING_BURN,
        WATER_DROP,
        WIND,
        WINDOW_BLIND,
        WINDOW_SLICE,
        WIPE_DOWN,
        WIPE_LEFT,
        WIPE_UP,
        WIPE_RIGHT,
        ZOOM_IN_CIRCLE
    }

    fun getTransitionByType(transitionType: TransitionType):GSTransition {
        return when(transitionType) {
            TransitionType.NONE -> GSTransition()
            TransitionType.POLKA_DOTS -> GSPolkaDotsTransition()
            TransitionType.WIPE_DOWN -> GSWipeDownTransition()
            TransitionType.ANGULAR -> GSAngularTransition()
            TransitionType.BOUNCE -> GSBounceTransition()
            TransitionType.BOW_TIE_HORIZONTAL -> GSBowTieHorizontalTransition()
            TransitionType.BOW_TIE_VERTICAL -> GSBowTieVerticalTransition()
            TransitionType.BUTTERFLY_WAVE -> GSButterflyWaveTransition()
            TransitionType.CANNABIS_LEAF -> GSCannabisLeafTransition()
            TransitionType.CIRCLE_CROP -> GSCircleCropTransition()
            TransitionType.CIRCLE -> GSCircleTransition()
            TransitionType.CIRCLE_OPEN -> GSCircleOpenTransition()
            TransitionType.COLOR_PHASE -> GSColorPhaseTransition()
            TransitionType.COLOR_DISTANCE -> GSColorDistanceTransition()
            TransitionType.CRAZY_PARAMETRIC -> GSCrazyParametricTransition()
            TransitionType.CROSS_HATCH -> GSCrossHatchTransition()
            TransitionType.CROSS_WARP -> GSCrossWarpTransition()
            TransitionType.CROSS_ZOOM -> GSCrossZoomTransition()
            TransitionType.CUBE -> GSCubeTransition()
            TransitionType.DIRECTION_WIPE -> GSDirectionWipeTransition()
            TransitionType.DIRECTION_WARP -> GSDirectionWarpTransition()
            TransitionType.DOOM_SCREEN -> GSDoomScreenTransition()
            TransitionType.DOOR_WAY -> GSDoorWayTransition()
            TransitionType.DREAMY -> GSDreamyTransition()
            TransitionType.FADE_GRAY_SCALE -> GSFadeGrayScaleTransition()
            TransitionType.FLY_EYE -> GSFlyEyeTransition()
            TransitionType.GLITCH -> GSGlitchTransition()
            TransitionType.GRID_FLIP -> GSGridFlipTransition()
            TransitionType.IN_HEART -> GSInHeartTransition()
            TransitionType.INVERTED_PAGE_CURL -> GSInvertedPageCurlTransition()
            TransitionType.KALE_IDO_SCOPE -> GSKaleIdoScopeTransition()
            TransitionType.LINEAR_BLUR -> GSLinearBlurTransition()
            TransitionType.MORPH -> GSMorphTransition()
            TransitionType.MOSAIC -> GSMosaicTransition()
            TransitionType.MULTIPLY_BLEND -> GSMultiplyBlendTransition()
            TransitionType.PER_LIN -> GSPerLinTransition()
            TransitionType.PIN_WHEEL -> GSPinWheelTransition()
            TransitionType.PIXEL_IZE -> GSPixelIzeTransition()
            TransitionType.POLAR_FUN -> GSPolarFunTransition()
            TransitionType.RADIAL -> GSRadialTransition()
            TransitionType.RANDOM_SQUARE -> GSRandomSquareTransition()
            TransitionType.RIPPLE -> GSRippleTransition()
            TransitionType.ROTATE_SCALE_FADE -> GSRotateScaleFadeTransition()
            TransitionType.SIMPLE_ZOOM -> GSSimpleZoomTransition()
            TransitionType.SQUARE_WIRE -> GSSquareWireTransition()
            TransitionType.SQUEEZE -> GSSqueezeTransition()
            TransitionType.SWAP -> GSSwapTransition()
            TransitionType.SWIRL -> GSSwirlTransition()
            TransitionType.UNDULATING_BURN -> GSUndulatingBurnTransition()
            TransitionType.WATER_DROP -> GSWaterDropTransition()
            TransitionType.WIND -> GSWindTransition()
            TransitionType.WINDOW_BLIND -> GSWindowBlindTransition()
            TransitionType.WINDOW_SLICE -> GSWindowSliceTransition()
            TransitionType.WIPE_LEFT -> GSWipeLeftTransition()
            TransitionType.WIPE_UP -> GSWipeUpTransition()
            TransitionType.WIPE_RIGHT -> GSWipeRightTransition()
            TransitionType.ZOOM_IN_CIRCLE -> GSZoomCircleTransition()
        }
    }

    fun createFragmentShaderCode(transitionCodeId:Int) :String{
        return RawResourceReader.readTextFileFromRawResource(VideoMakerApplication.getContext(), transitionCodeId)
    }

    fun getGSTransitionList():ArrayList<GSTransition> {
        val gsTransitionList = ArrayList<GSTransition>()
        for(value in TransitionType.values()) {
            gsTransitionList.add(getTransitionByType(value))
        }

        return gsTransitionList
    }



}