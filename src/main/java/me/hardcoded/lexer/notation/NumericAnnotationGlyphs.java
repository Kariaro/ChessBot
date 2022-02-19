package me.hardcoded.lexer.notation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum NumericAnnotationGlyphs {
	NAG_NULL(0, "null annotation"),
	NAG_1(1, "good move (traditional '!')"),
	NAG_2(2, "poor move (traditional '?')"),
	NAG_3(3, "very good move (traditional '!!')"),
	NAG_4(4, "very poor move (traditional '??')"),
	NAG_5(5, "speculative move (traditional '!?')"),
	NAG_6(6, "questionable move (traditional '?!')"),
	NAG_7(7, "forced move (all others lose quickly)"),
	NAG_8(8, "singular move (no reasonable alternatives)"),
	NAG_9(9, "worst move"),
	NAG_10(10, "drawish position"),
	NAG_11(11, "equal chances, quiet position"),
	NAG_12(12, "equal chances, active position"),
	NAG_13(13, "unclear position"),
	NAG_14(14, "White has a slight advantage"),
	NAG_15(15, "Black has a slight advantage"),
	NAG_16(16, "White has a moderate advantage"),
	NAG_17(17, "Black has a moderate advantage"),
	NAG_18(18, "White has a decisive advantage"),
	NAG_19(19, "Black has a decisive advantage"),
	NAG_20(20, "White has a crushing advantage (Black should resign)"),
	NAG_21(21, "Black has a crushing advantage (White should resign)"),
	NAG_22(22, "White is in zugzwang"),
	NAG_23(23, "Black is in zugzwang"),
	NAG_24(24, "White has a slight space advantage"),
	NAG_25(25, "Black has a slight space advantage"),
	NAG_26(26, "White has a moderate space advantage"),
	NAG_27(27, "Black has a moderate space advantage"),
	NAG_28(28, "White has a decisive space advantage"),
	NAG_29(29, "Black has a decisive space advantage"),
	NAG_30(30, "White has a slight time (development) advantage"),
	NAG_31(31, "Black has a slight time (development) advantage"),
	NAG_32(32, "White has a moderate time (development) advantage"),
	NAG_33(33, "Black has a moderate time (development) advantage"),
	NAG_34(34, "White has a decisive time (development) advantage"),
	NAG_35(35, "Black has a decisive time (development) advantage"),
	NAG_36(36, "White has the initiative"),
	NAG_37(37, "Black has the initiative"),
	NAG_38(38, "White has a lasting initiative"),
	NAG_39(39, "Black has a lasting initiative"),
	NAG_40(40, "White has the attack"),
	NAG_41(41, "Black has the attack"),
	NAG_42(42, "White has insufficient compensation for material deficit"),
	NAG_43(43, "Black has insufficient compensation for material deficit"),
	NAG_44(44, "White has sufficient compensation for material deficit"),
	NAG_45(45, "Black has sufficient compensation for material deficit"),
	NAG_46(46, "White has more than adequate compensation for material deficit"),
	NAG_47(47, "Black has more than adequate compensation for material deficit"),
	NAG_48(48, "White has a slight center control advantage"),
	NAG_49(49, "Black has a slight center control advantage"),
	NAG_50(50, "White has a moderate center control advantage"),
	NAG_51(51, "Black has a moderate center control advantage"),
	NAG_52(52, "White has a decisive center control advantage"),
	NAG_53(53, "Black has a decisive center control advantage"),
	NAG_54(54, "White has a slight kingside control advantage"),
	NAG_55(55, "Black has a slight kingside control advantage"),
	NAG_56(56, "White has a moderate kingside control advantage"),
	NAG_57(57, "Black has a moderate kingside control advantage"),
	NAG_58(58, "White has a decisive kingside control advantage"),
	NAG_59(59, "Black has a decisive kingside control advantage"),
	NAG_60(60, "White has a slight queenside control advantage"),
	NAG_61(61, "Black has a slight queenside control advantage"),
	NAG_62(62, "White has a moderate queenside control advantage"),
	NAG_63(63, "Black has a moderate queenside control advantage"),
	NAG_64(64, "White has a decisive queenside control advantage"),
	NAG_65(65, "Black has a decisive queenside control advantage"),
	NAG_66(66, "White has a vulnerable first rank"),
	NAG_67(67, "Black has a vulnerable first rank"),
	NAG_68(68, "White has a well protected first rank"),
	NAG_69(69, "Black has a well protected first rank"),
	NAG_70(70, "White has a poorly protected king"),
	NAG_71(71, "Black has a poorly protected king"),
	NAG_72(72, "White has a well protected king"),
	NAG_73(73, "Black has a well protected king"),
	NAG_74(74, "White has a poorly placed king"),
	NAG_75(75, "Black has a poorly placed king"),
	NAG_76(76, "White has a well placed king"),
	NAG_77(77, "Black has a well placed king"),
	NAG_78(78, "White has a very weak pawn structure"),
	NAG_79(79, "Black has a very weak pawn structure"),
	NAG_80(80, "White has a moderately weak pawn structure"),
	NAG_81(81, "Black has a moderately weak pawn structure"),
	NAG_82(82, "White has a moderately strong pawn structure"),
	NAG_83(83, "Black has a moderately strong pawn structure"),
	NAG_84(84, "White has a very strong pawn structure"),
	NAG_85(85, "Black has a very strong pawn structure"),
	NAG_86(86, "White has poor knight placement"),
	NAG_87(87, "Black has poor knight placement"),
	NAG_88(88, "White has good knight placement"),
	NAG_89(89, "Black has good knight placement"),
	NAG_90(90, "White has poor bishop placement"),
	NAG_91(91, "Black has poor bishop placement"),
	NAG_92(92, "White has good bishop placement"),
	NAG_93(93, "Black has good bishop placement"),
	NAG_94(94, "White has poor rook placement"),
	NAG_95(95, "Black has poor rook placement"),
	NAG_96(96, "White has good rook placement"),
	NAG_97(97, "Black has good rook placement"),
	NAG_98(98, "White has poor queen placement"),
	NAG_99(99, "Black has poor queen placement"),
	NAG_100(100, "White has good queen placement"),
	NAG_101(101, "Black has good queen placement"),
	NAG_102(102, "White has poor piece coordination"),
	NAG_103(103, "Black has poor piece coordination"),
	NAG_104(104, "White has good piece coordination"),
	NAG_105(105, "Black has good piece coordination"),
	NAG_106(106, "White has played the opening very poorly"),
	NAG_107(107, "Black has played the opening very poorly"),
	NAG_108(108, "White has played the opening poorly"),
	NAG_109(109, "Black has played the opening poorly"),
	NAG_110(110, "White has played the opening well"),
	NAG_111(111, "Black has played the opening well"),
	NAG_112(112, "White has played the opening very well"),
	NAG_113(113, "Black has played the opening very well"),
	NAG_114(114, "White has played the middlegame very poorly"),
	NAG_115(115, "Black has played the middlegame very poorly"),
	NAG_116(116, "White has played the middlegame poorly"),
	NAG_117(117, "Black has played the middlegame poorly"),
	NAG_118(118, "White has played the middlegame well"),
	NAG_119(119, "Black has played the middlegame well"),
	NAG_120(120, "White has played the middlegame very well"),
	NAG_121(121, "Black has played the middlegame very well"),
	NAG_122(122, "White has played the ending very poorly"),
	NAG_123(123, "Black has played the ending very poorly"),
	NAG_124(124, "White has played the ending poorly"),
	NAG_125(125, "Black has played the ending poorly"),
	NAG_126(126, "White has played the ending well"),
	NAG_127(127, "Black has played the ending well"),
	NAG_128(128, "White has played the ending very well"),
	NAG_129(129, "Black has played the ending very well"),
	NAG_130(130, "White has slight counterplay"),
	NAG_131(131, "Black has slight counterplay"),
	NAG_132(132, "White has moderate counterplay"),
	NAG_133(133, "Black has moderate counterplay"),
	NAG_134(134, "White has decisive counterplay"),
	NAG_135(135, "Black has decisive counterplay"),
	NAG_136(136, "White has moderate time control pressure"),
	NAG_137(137, "Black has moderate time control pressure"),
	NAG_138(138, "White has severe time control pressure"),
	NAG_139(139, "Black has severe time control pressure");
	
	private static final Map<Integer, NumericAnnotationGlyphs> VALUES = Arrays.stream(values())
		.collect(Collectors.toMap(NumericAnnotationGlyphs::getId, v -> v));
	
	private final int id;
	private final String comment;
	
	NumericAnnotationGlyphs(int id, String comment) {
		this.id = id;
		this.comment = comment;
	}
	
	public int getId() {
		return id;
	}
	
	public String getComment() {
		return comment;
	}
	
	public static boolean hasNotation(int id) {
		return VALUES.containsKey(id);
	}
	
	public static NumericAnnotationGlyphs getNotation(int id) {
		return VALUES.get(id);
	}
}