package net.runelite.client.plugins.spoontob.rooms.Nylocas;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.api.util.Text;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.spoontob.Room;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.util.TheatreInputListener;
import net.runelite.client.plugins.spoontob.util.TheatreRegions;
import net.runelite.client.plugins.spoontob.util.WeaponMap;
import net.runelite.client.plugins.spoontob.util.WeaponStyle;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;

public class Nylocas extends Room {
    private static final Logger log = LoggerFactory.getLogger(SpoonTobPlugin.class);
    @Inject
    private SkillIconManager skillIconManager;
    @Inject
    private MouseManager mouseManager;
    @Inject
    private TheatreInputListener theatreInputListener;
    @Inject
    private Client client;
    @Inject
    private NylocasOverlay nylocasOverlay;
    @Inject
    public NylocasAliveCounterOverlay nylocasAliveCounterOverlay;
    @Inject
    private NyloTimer nyloTimer;
    @Inject
    private NyloWaveSpawnInfobox waveSpawnInfobox;

    private static final int NPCID_NYLOCAS_PILLAR = 8358;
    private static final int NPCID_NYLOCAS_SM_PILLAR = 10790;
	private static final int NPCID_NYLOCAS_HM_PILLAR = 10811;
    private static final int NYLO_MAP_REGION = 13122;
    private static final int BLOAT_MAP_REGION = 13125;
    private static final String MAGE_NYLO = "Nylocas Hagios";
    private static final String RANGE_NYLO = "Nylocas Toxobolos";
    private static final String MELEE_NYLO = "Nylocas Ischyros";
    private static final String BOSS_NYLO = "Nylocas Vasilias";
    private static final String DEMIBOSS_NYLO = "Nylocas Prinkipas";

    @Getter
    @Setter
    private static Runnable wave31Callback = null;
    @Getter
    @Setter
    private static Runnable endOfWavesCallback = null;

    @Getter
    private boolean nyloActive;

    public int nyloWave = 0;
    private int varbit6447 = -1;
    @Getter
    private Instant nyloWaveStart;
    @Getter
    private NyloSelectionManager nyloSelectionManager;

    @Getter
    private HashMap<NPC, Integer> nylocasPillars = new HashMap();
    @Getter
    private HashMap<NPC, Integer> nylocasNpcs = new HashMap();
    @Getter
    private HashSet<NPC> aggressiveNylocas = new HashSet();
    private HashMap<NyloNPC, NPC> currentWave = new HashMap();

    private int ticksSinceLastWave = 0;
    @Getter
    public int instanceTimer = 0;
    @Getter
    private boolean isInstanceTimerRunning = false;
    private boolean nextInstance = true;

    private int rangeBoss = 0;
    private int mageBoss = 0;
    private int meleeBoss = 0;
    private int rangeSplits = 0;
    private int mageSplits = 0;
    private int meleeSplits = 0;
    private int preRangeSplits = 0;
    private int preMageSplits = 0;
    private int preMeleeSplits = 0;
    private int postRangeSplits = 0;
    private int postMageSplits = 0;
    private int postMeleeSplits = 0;

    @Getter
    private int bossChangeTicks;
    private int lastBossId;
    @Getter
    private NPC nylocasBoss;
    private boolean nyloBossAlive;

    public int weaponId = 0;
    public ArrayList<Integer> magicWeaponId = new ArrayList<Integer>(Arrays.asList(
            12899, 20736, 2417, 2416, 2415, 22323, 6562, 11998, 4675, 22292, 21006, 6914, 12422, 6912, 6910, 6908, 1393, 3053, 11787, 20730, 1401, 3054,
            24422, 24423, 24424, 24425, 11905, 11907, 22288, 25731, 4710, 4862, 4863, 4864, 4865));
    public ArrayList<Integer> rangeWeaponId = new ArrayList<Integer>(Arrays.asList(
            12926, 20997, 12788, 11235, 19478, 19481, 21012, 21902, 11785, 10156, 9185, 8880, 4934, 4935, 4936, 4937, 11959, 9977, 861,
            4212, 4214, 4215, 4216, 4217, 4218, 4219, 4220, 4221, 4222, 4223, 11748, 11749, 11750, 11751, 11752, 11753, 11754, 11755, 11756, 11757, 11758,
            806, 807, 808, 809, 810, 811, 11230, 11959, 10034, 25862, 25865, 25867, 25869, 25884, 25886, 25888, 25890, 25892, 25894, 25896, 4734, 4934, 3935, 4936, 4937));
    public ArrayList<Integer> meleeWeaponId = new ArrayList<Integer>(Arrays.asList(
            23360, 12006, 22324, 22325, 13576, 22978, 21003, 23987, 20370, 4587, 21009, 12809, 19675, 21219, 21015, 4910, 4982, 20727, 4153, 10887, 5698, 3204, 11824,
            11802, 18804, 11806, 11808, 20366, 20368, 20372, 20374, 21646, 21742, 1333, 20000, 11889, 22731, 22734, 22486, 11838, 13263, 4151, 12773, 12774, 22840, 11037, 23995,
            23997, 24219, 24551, 24553, 13652, 1215, 1231, 24417, 22542, 22545, 11804, -1, 25739, 25741, 25736, 25738, 25734, 25870, 25872, 25874, 25876, 25878, 25880, 25882,
            4718, 4886, 4887, 4888, 4889, 4755, 4982, 4983, 4984, 4985, 4726, 4910, 4911, 4912, 4913, 4747, 4958, 4959, 4960, 4961, 12727, 22613, 22615, 24617, 24619));

    private static final Set<Point> spawnTiles = ImmutableSet.of(
            new Point(17, 24), new Point(17, 25), new Point(31, 9), new Point(32, 9), new Point(46, 24), new Point(46, 25));

    @Getter
    private final Map<NPC, Integer> splitsMap = new HashMap<>();
    private final Set<NPC> bigNylos = new HashSet<>();

    public boolean showHint;
	
	public final ArrayList<Color> meleeNyloRaveColors = new ArrayList<Color>();
    public final ArrayList<Color> rangeNyloRaveColors = new ArrayList<Color>();
    public final ArrayList<Color> mageNyloRaveColors = new ArrayList<Color>();

    public String tobMode = "";
	public boolean minibossAlive = false;
	public NPC nyloMiniboss = null;
    public String nyloBossStyle = "";
	
	public int logTicks = 0;

	public int waveSpawnTicks = 0;
	public boolean stalledWave = false;

	private boolean mirrorMode;
    private boolean setAlive;

    private WeaponStyle weaponStyle;
    private boolean skipTickCheck = false;

    @Inject
    protected Nylocas(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super(plugin, config);
    }

    public void init() {
        InfoBoxComponent box = new InfoBoxComponent();
        box.setImage(skillIconManager.getSkillImage(Skill.ATTACK));
        NyloSelectionBox nyloMeleeOverlay = new NyloSelectionBox(box);
        nyloMeleeOverlay.setSelected(config.getHighlightMeleeNylo());
        box = new InfoBoxComponent();
        box.setImage(skillIconManager.getSkillImage(Skill.MAGIC));
        NyloSelectionBox nyloMageOverlay = new NyloSelectionBox(box);
        nyloMageOverlay.setSelected(config.getHighlightMageNylo());
        box = new InfoBoxComponent();
        box.setImage(skillIconManager.getSkillImage(Skill.RANGED));
        NyloSelectionBox nyloRangeOverlay = new NyloSelectionBox(box);
        nyloRangeOverlay.setSelected(config.getHighlightRangeNylo());
        nyloSelectionManager = new NyloSelectionManager(nyloMeleeOverlay, nyloMageOverlay, nyloRangeOverlay);
        nyloSelectionManager.setHidden(!config.nyloOverlay());
        nylocasAliveCounterOverlay.setHidden(!config.nyloAlivePanel());
        nylocasAliveCounterOverlay.setNyloAlive(0);
        nylocasAliveCounterOverlay.setMaxNyloAlive(12);
        nyloBossAlive = false;
        tobMode = "";
		minibossAlive = false;
		nyloMiniboss = null;
		nyloBossStyle = "";
		waveSpawnTicks = 0;
		stalledWave = false;
    }

    private void startupNyloOverlay() {
        mouseManager.registerMouseListener(theatreInputListener);
        if (nyloSelectionManager != null) {
            overlayManager.add(nyloSelectionManager);
            nyloSelectionManager.setHidden(!config.nyloOverlay());
        }

        if (nylocasAliveCounterOverlay != null) {
            overlayManager.add(nylocasAliveCounterOverlay);
            nylocasAliveCounterOverlay.setHidden(!config.nyloAlivePanel());
        }
    }

    private void shutdownNyloOverlay() {
        mouseManager.unregisterMouseListener(theatreInputListener);
        if (nyloSelectionManager != null) {
            overlayManager.remove(nyloSelectionManager);
            nyloSelectionManager.setHidden(true);
        }

        if (nylocasAliveCounterOverlay != null) {
            overlayManager.remove(nylocasAliveCounterOverlay);
            nylocasAliveCounterOverlay.setHidden(true);
        }
    }

    public void load() {
        overlayManager.add(nylocasOverlay);
        overlayManager.add(nyloTimer);
        overlayManager.add(waveSpawnInfobox);
        bossChangeTicks = -1;
        lastBossId = -1;
        weaponStyle = null;
    }

    public void unload() {
        overlayManager.remove(nylocasOverlay);
        overlayManager.remove(nyloTimer);
        overlayManager.remove(waveSpawnInfobox);
        shutdownNyloOverlay();
        nyloBossAlive = false;
        nyloWaveStart = null;
        nyloActive = false;
        tobMode = "";
		minibossAlive = false;
        nyloBossStyle = "";
		logTicks = 0;
        waveSpawnTicks = 0;
        stalledWave = false;
        weaponStyle = null;
        splitsMap.clear();
        bigNylos.clear();
    }

    private void resetNylo() {
        nyloBossAlive = false;
        nylocasPillars.clear();
        nylocasNpcs.clear();
        aggressiveNylocas.clear();
        setNyloWave(0);
        currentWave.clear();
        bossChangeTicks = -1;
        lastBossId = -1;
        nylocasBoss = null;
        weaponId = 0;
        weaponStyle = null;
        splitsMap.clear();
        bigNylos.clear();

        tobMode = "";
		minibossAlive = false;
		nyloMiniboss = null;
        nyloBossStyle = "";
		logTicks = 0;
		waveSpawnTicks = 0;
		stalledWave = false;
    }

    private void setNyloWave(int wave) {
        nyloWave = wave;
        nylocasAliveCounterOverlay.setWave(wave);
        if (wave >= 3) {
            isInstanceTimerRunning = false;
        }

        if (wave != 0) {
            if (tobMode.equals("hard")) {
                ticksSinceLastWave = ((NylocasWave)NylocasWave.hmWaves.get(wave)).getWaveDelay();
            }else if (tobMode.equals("story")) {
                ticksSinceLastWave = ((NylocasWave)NylocasWave.smWaves.get(wave)).getWaveDelay();
            }else if (tobMode.equals("normal")) {
                ticksSinceLastWave = ((NylocasWave)NylocasWave.smWaves.get(wave)).getWaveDelay();
            }
        }

        if (wave >= 20 && nylocasAliveCounterOverlay.getMaxNyloAlive() != 24) {
            nylocasAliveCounterOverlay.setMaxNyloAlive(24);
        }

        if (wave < 20 && nylocasAliveCounterOverlay.getMaxNyloAlive() != 12) {
            nylocasAliveCounterOverlay.setMaxNyloAlive(12);
        }

        if (wave == 31 && wave31Callback != null) {
            wave31Callback.run();
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged change) {
        if (change.getKey().equals("nyloOverlay")) {
            nyloSelectionManager.setHidden(!config.nyloOverlay());
        }else if (change.getKey().equals("nyloAliveCounter")) {
            nylocasAliveCounterOverlay.setHidden(!config.nyloAlivePanel());
        }else if (change.getKey().equals("showLowestPillar") && !config.showLowestPillar()) {
            client.clearHintArrow();
        }else if(change.getKey().equals("hidePillars")){
            plugin.refreshScene();
            if(config.hidePillars() == SpoonTobConfig.hidePillarsMode.PILLARS){
                removeGameObjectsFromScene(ImmutableSet.of(32862), 0);
            }else if(config.hidePillars() == SpoonTobConfig.hidePillarsMode.CLEAN){
                removeGameObjectsFromScene(ImmutableSet.of(32862, 32876, 32899), 0);
            }

            if(config.hideEggs()) {
                removeGameObjectsFromScene(ImmutableSet.of(32939, 32937, 2739, 32865), 0);
            }
        }else if(change.getKey().equals("hideEggs")){
            plugin.refreshScene();
            if(config.hideEggs()) {
                removeGameObjectsFromScene(ImmutableSet.of(32939, 32937, 2739, 32865), 0);
            }

            if(config.hidePillars() == SpoonTobConfig.hidePillarsMode.PILLARS){
                removeGameObjectsFromScene(ImmutableSet.of(32862), 0);
            }else if(config.hidePillars() == SpoonTobConfig.hidePillarsMode.CLEAN){
                removeGameObjectsFromScene(ImmutableSet.of(32862, 32876, 32899), 0);
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        int id = npc.getId();
        switch(npc.getId()) {
            case NpcID.NYLOCAS_ISCHYROS_8342:
            case NpcID.NYLOCAS_TOXOBOLOS_8343:
            case NpcID.NYLOCAS_HAGIOS:
            case NpcID.NYLOCAS_ISCHYROS_8345:
            case NpcID.NYLOCAS_TOXOBOLOS_8346:
            case NpcID.NYLOCAS_HAGIOS_8347:
            case NpcID.NYLOCAS_ISCHYROS_8348:
            case NpcID.NYLOCAS_TOXOBOLOS_8349:
            case NpcID.NYLOCAS_HAGIOS_8350:
            case NpcID.NYLOCAS_ISCHYROS_8351:
            case NpcID.NYLOCAS_TOXOBOLOS_8352:
            case NpcID.NYLOCAS_HAGIOS_8353:
            case NpcID.NYLOCAS_ISCHYROS_10774: //Story Mode
            case NpcID.NYLOCAS_TOXOBOLOS_10775:
            case NpcID.NYLOCAS_HAGIOS_10776:
            case NpcID.NYLOCAS_ISCHYROS_10777:
            case NpcID.NYLOCAS_TOXOBOLOS_10778:
            case NpcID.NYLOCAS_HAGIOS_10779:
            case NpcID.NYLOCAS_ISCHYROS_10780:
            case NpcID.NYLOCAS_TOXOBOLOS_10781:
            case NpcID.NYLOCAS_HAGIOS_10782:
            case NpcID.NYLOCAS_ISCHYROS_10783:
            case NpcID.NYLOCAS_TOXOBOLOS_10784:
            case NpcID.NYLOCAS_HAGIOS_10785:
            case NpcID.NYLOCAS_ISCHYROS_10791: //Hard Mode
            case NpcID.NYLOCAS_TOXOBOLOS_10792:
            case NpcID.NYLOCAS_HAGIOS_10793:
            case NpcID.NYLOCAS_ISCHYROS_10794:
            case NpcID.NYLOCAS_TOXOBOLOS_10795:
            case NpcID.NYLOCAS_HAGIOS_10796:
            case NpcID.NYLOCAS_ISCHYROS_10797:
            case NpcID.NYLOCAS_TOXOBOLOS_10798:
            case NpcID.NYLOCAS_HAGIOS_10799:
            case NpcID.NYLOCAS_ISCHYROS_10800:
            case NpcID.NYLOCAS_TOXOBOLOS_10801:
            case NpcID.NYLOCAS_HAGIOS_10802:
            case NpcID.NYLOCAS_PRINKIPAS:
            case NpcID.NYLOCAS_PRINKIPAS_10804:
            case NpcID.NYLOCAS_PRINKIPAS_10805:
            case NpcID.NYLOCAS_PRINKIPAS_10806:
                if (nyloActive) {
					if(npc.getId() == 10804){
						minibossAlive = true;
						nyloMiniboss = npc;
						bossChangeTicks = 10;
					}else {
						nylocasNpcs.put(npc, 52);
					}
					
					if(minibossAlive){
						nylocasAliveCounterOverlay.setNyloAlive(nylocasNpcs.size() + 3);
					}else {
						nylocasAliveCounterOverlay.setNyloAlive(nylocasNpcs.size());
					}
                    NyloNPC nyloNPC = matchNpc(npc);
                    if (nyloNPC != null) {
                        currentWave.put(nyloNPC, npc);
                        if (currentWave.size() > 2) {
                            matchWave();
                        }
                    }
                }
                setAlive = true;
                break;
            case NpcID.NYLOCAS_VASILIAS:
            case NpcID.NYLOCAS_VASILIAS_8355:
            case NpcID.NYLOCAS_VASILIAS_8356:
            case NpcID.NYLOCAS_VASILIAS_8357:
            case NpcID.NYLOCAS_VASILIAS_10786: //Story mode
            case NpcID.NYLOCAS_VASILIAS_10787:
            case NpcID.NYLOCAS_VASILIAS_10788:
            case NpcID.NYLOCAS_VASILIAS_10789:
            case NpcID.NYLOCAS_VASILIAS_10807: //Hard mode
            case NpcID.NYLOCAS_VASILIAS_10808:
            case NpcID.NYLOCAS_VASILIAS_10809:
            case NpcID.NYLOCAS_VASILIAS_10810:
                showHint = false;
                isInstanceTimerRunning = false;
                nyloBossStyle = "melee";
                client.clearHintArrow();
                nyloBossAlive = true;
                lastBossId = id;
                nylocasBoss = npc;
                meleeBoss = 0;
                mageBoss = 0;
                rangeBoss = 0;
                if (npc.getId() == 8355 || npc.getId() == 10787 || npc.getId() == 10808) {
                    if (npc.getId() == 10787) {
                        bossChangeTicks = 15;
                    } else {
                        bossChangeTicks = 10;
                    }
                    meleeBoss++;
                }
                break;
            case NPCID_NYLOCAS_PILLAR:
            case NPCID_NYLOCAS_SM_PILLAR: //Story Mode
			case NPCID_NYLOCAS_HM_PILLAR: //Hard Mode
                nyloActive = true;
                showHint = true;
                if (nylocasPillars.size() > 3) {
                    nylocasPillars.clear();
                }
                if (!nylocasPillars.containsKey(npc)) {
                    nylocasPillars.put(npc, 100);
                }

                if(npc.getId() == 10811){
                    tobMode = "hard";
                }else if(npc.getId() == 10790){
                    tobMode = "story";
                }else {
					tobMode = "normal";
				}

                mageSplits = 0;
                rangeSplits = 0;
                meleeSplits = 0;
                preRangeSplits = 0;
                preMageSplits = 0;
                preMeleeSplits = 0;
                postRangeSplits = 0;
                postMageSplits = 0;
                postMeleeSplits = 0;
        }

        if(nyloActive) {
            switch (id) {
                case NpcID.NYLOCAS_ISCHYROS_8345: //Normal mode
                case NpcID.NYLOCAS_TOXOBOLOS_8346:
                case NpcID.NYLOCAS_HAGIOS_8347:
                case NpcID.NYLOCAS_ISCHYROS_10777: //Story mode
                case NpcID.NYLOCAS_TOXOBOLOS_10778:
                case NpcID.NYLOCAS_HAGIOS_10779:
                case NpcID.NYLOCAS_ISCHYROS_10794: //Hard mode
                case NpcID.NYLOCAS_TOXOBOLOS_10795:
                case NpcID.NYLOCAS_HAGIOS_10796:
                    bigNylos.add(npc);
                    break;
            }

            WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
            Point spawnLoc = new Point(worldPoint.getRegionX(), worldPoint.getRegionY());
            if (!spawnTiles.contains(spawnLoc)) {
                if (npc.getName() != null) {
                    if (npc.getName().contains("Hagios") && (npc.getId() == 8344 || npc.getId() == 10776 || npc.getId() == 10793)) {
                        mageSplits++;
                        if (nyloWave < 20) {
                            preMageSplits++;
                        } else {
                            postMageSplits++;
                        }
                    } else if (npc.getName().contains("Toxobolos") && (npc.getId() == 8343 || npc.getId() == 10775 || npc.getId() == 10792)) {
                        rangeSplits++;
                        if (nyloWave < 20) {
                            preRangeSplits++;
                        } else {
                            postRangeSplits++;
                        }
                    } else if (npc.getName().contains("Ischyros") && (npc.getId() == 8342 || npc.getId() == 10774 || npc.getId() == 10791)) {
                        meleeSplits++;
                        if (nyloWave < 20) {
                            preMeleeSplits++;
                        } else {
                            postMeleeSplits++;
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event){
        NPC npc = event.getNpc();
        int id = npc.getId();
        if (id == 8355 || id == 8356 || id == 8357 || id == 10787 || id == 10788 || id == 10789 || id == 10808 || id == 10809 || id == 10810 || id == 10804 || id == 10805 || id == 10806) {
            if(id == 10787 || id == 10788 || id == 10789){
                bossChangeTicks = 16;
            }else {
                bossChangeTicks = 11;
            }
            lastBossId = id;

            if(id == 10804 || id == 10805 || id == 10806){
                nyloMiniboss = npc;
            }
        } 
		
		if (id == 8355 || id == 10787 || id == 10808) {
            meleeBoss++;
            nyloBossStyle = "melee";
        } else if (id == 8356 || id == 10788 || id == 10809) {
            mageBoss++;
            nyloBossStyle = "mage";
        } else if (id == 8357 || id == 10789 || id == 10810) {
            rangeBoss++;
            nyloBossStyle = "range";
        }
    }

    private void matchWave() {
        HashSet<NyloNPC> potentialWave = null;
        Set<NyloNPC> currentWaveKeySet = currentWave.keySet();

        for (int wave = nyloWave + 1; wave <= NylocasWave.MAX_WAVE; wave++) {
            boolean matched = true;
            if (tobMode.equals("hard")) {
                potentialWave = ((NylocasWave) NylocasWave.hmWaves.get(wave)).getWaveData();
            } else if (tobMode.equals("story")) {
                potentialWave = ((NylocasWave) NylocasWave.smWaves.get(wave)).getWaveData();
            } else if (tobMode.equals("normal")) {
                potentialWave = ((NylocasWave) NylocasWave.waves.get(wave)).getWaveData();
            }

            for (NyloNPC nyloNpc : potentialWave) {
                if (!currentWaveKeySet.contains(nyloNpc)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                setNyloWave(wave);
                stalledWave = false;
                if(ticksSinceLastWave > 0) {
                    waveSpawnTicks = ticksSinceLastWave;
                } else {
                    waveSpawnTicks = 4;
                }

                for (NyloNPC nyloNPC : potentialWave) {
                    if (nyloNPC.isAggressive()) {
                        aggressiveNylocas.add(currentWave.get(nyloNPC));
                    }
                }

                currentWave.clear();
                return;
            }
        }
    }

    private NyloNPC matchNpc(NPC npc) {
        WorldPoint p = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
        Point point = new Point(p.getRegionX(), p.getRegionY());
        NylocasSpawnPoint spawnPoint = NylocasSpawnPoint.getLookupMap().get(point);

        if (spawnPoint == null) {
            return null;
        }

        NylocasType nylocasType = NylocasType.getLookupMap().get(npc.getId());

        if (nylocasType == null) {
            return null;
        }

        return new NyloNPC(nylocasType, spawnPoint);
    }


    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        int id = npc.getId();
        switch(npc.getId()) {
            case NpcID.NYLOCAS_ISCHYROS_8342:
            case NpcID.NYLOCAS_TOXOBOLOS_8343:
            case NpcID.NYLOCAS_HAGIOS:
            case NpcID.NYLOCAS_ISCHYROS_8345:
            case NpcID.NYLOCAS_TOXOBOLOS_8346:
            case NpcID.NYLOCAS_HAGIOS_8347:
            case NpcID.NYLOCAS_ISCHYROS_8348:
            case NpcID.NYLOCAS_TOXOBOLOS_8349:
            case NpcID.NYLOCAS_HAGIOS_8350:
            case NpcID.NYLOCAS_ISCHYROS_8351:
            case NpcID.NYLOCAS_TOXOBOLOS_8352:
            case NpcID.NYLOCAS_HAGIOS_8353:
            case NpcID.NYLOCAS_ISCHYROS_10774: //Story Mode
            case NpcID.NYLOCAS_TOXOBOLOS_10775:
            case NpcID.NYLOCAS_HAGIOS_10776:
            case NpcID.NYLOCAS_ISCHYROS_10777:
            case NpcID.NYLOCAS_TOXOBOLOS_10778:
            case NpcID.NYLOCAS_HAGIOS_10779:
            case NpcID.NYLOCAS_ISCHYROS_10780:
            case NpcID.NYLOCAS_TOXOBOLOS_10781:
            case NpcID.NYLOCAS_HAGIOS_10782:
            case NpcID.NYLOCAS_ISCHYROS_10783:
            case NpcID.NYLOCAS_TOXOBOLOS_10784:
            case NpcID.NYLOCAS_HAGIOS_10785:
            case NpcID.NYLOCAS_ISCHYROS_10791: //Hard Mode
            case NpcID.NYLOCAS_TOXOBOLOS_10792:
            case NpcID.NYLOCAS_HAGIOS_10793:
            case NpcID.NYLOCAS_ISCHYROS_10794:
            case NpcID.NYLOCAS_TOXOBOLOS_10795:
            case NpcID.NYLOCAS_HAGIOS_10796:
            case NpcID.NYLOCAS_ISCHYROS_10797:
            case NpcID.NYLOCAS_TOXOBOLOS_10798:
            case NpcID.NYLOCAS_HAGIOS_10799:
            case NpcID.NYLOCAS_ISCHYROS_10800:
            case NpcID.NYLOCAS_TOXOBOLOS_10801:
            case NpcID.NYLOCAS_HAGIOS_10802:
            case NpcID.NYLOCAS_PRINKIPAS_10804:
            case NpcID.NYLOCAS_PRINKIPAS_10805:
            case NpcID.NYLOCAS_PRINKIPAS_10806:
                if (nylocasNpcs.remove(npc) != null || (npc.getId() == 10804 || npc.getId() == 10805 || npc.getId() == 10806)) {
					if(npc.getId() == 10804 || npc.getId() == 10805 || npc.getId() == 10806){
						nyloMiniboss = null;
						minibossAlive = false;
						bossChangeTicks = -1;
					}
					
					if(minibossAlive){
						nylocasAliveCounterOverlay.setNyloAlive(nylocasNpcs.size() + 3);
					}else {
						nylocasAliveCounterOverlay.setNyloAlive(nylocasNpcs.size());
					}
                }

                aggressiveNylocas.remove(npc);
                if (nyloWave == 31 && nylocasNpcs.size() == 0) {
                    if ((config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.WAVES || config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOTH)
                            && config.splitMsgTiming() == SpoonTobConfig.splitsMsgTiming.CLEANUP) {
                        if (config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.CAP || config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH) {
                            client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Pre-cap splits: <col=00FFFF>" + preMageSplits + "</col> - <col=00FF00>"
                                    + preRangeSplits + "</col> - <col=ff0000>" + preMeleeSplits + "</col> Post-cap splits: <col=00FFFF>" + postMageSplits + "</col> - <col=00FF00>"
                                    + postRangeSplits + "</col> - <col=ff0000>" + postMeleeSplits, null);
                        }
                        if (config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.TOTAL || config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH) {
                            client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Small splits: <col=00FFFF>" + mageSplits + "</col> - <col=00FF00>"
                                    + rangeSplits + "</col> - <col=ff0000>" + meleeSplits + "</col> ", null);
                        }
                    }
                    if (endOfWavesCallback != null) {
                        endOfWavesCallback.run();
                    }
                }
                setAlive = false;
                break;
            case NpcID.NYLOCAS_VASILIAS:
            case NpcID.NYLOCAS_VASILIAS_8355:
            case NpcID.NYLOCAS_VASILIAS_8356:
            case NpcID.NYLOCAS_VASILIAS_8357:
            case NpcID.NYLOCAS_VASILIAS_10786: //Story mode
            case NpcID.NYLOCAS_VASILIAS_10787:
            case NpcID.NYLOCAS_VASILIAS_10788:
            case NpcID.NYLOCAS_VASILIAS_10789:
            case NpcID.NYLOCAS_VASILIAS_10807: //Hard mode
            case NpcID.NYLOCAS_VASILIAS_10808:
            case NpcID.NYLOCAS_VASILIAS_10809:
            case NpcID.NYLOCAS_VASILIAS_10810:
                nyloBossAlive = false;
                nylocasBoss = null;
                break;
            case NPCID_NYLOCAS_PILLAR:
            case NPCID_NYLOCAS_SM_PILLAR: //Story Mode
            case NPCID_NYLOCAS_HM_PILLAR: //Hard Mode
                if (nylocasPillars.containsKey(npc)) {
                    nylocasPillars.remove(npc);
                }

                if (nylocasPillars.size() < 1) {
                    nyloWaveStart = null;
                    nyloActive = false;
                }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        int[] varps = client.getVarps();
        int newVarbit6447 = client.getVarbitValue(varps, 6447);
        if (isInNyloRegion() && newVarbit6447 != 0 && newVarbit6447 != varbit6447) {
            nyloWaveStart = Instant.now();
            if (nylocasAliveCounterOverlay != null) {
                nylocasAliveCounterOverlay.setNyloWaveStart(nyloWaveStart);
            }
        }

        if (TheatreRegions.inRegion(client, TheatreRegions.NYLOCAS)) {
            nyloActive = client.getVarbitValue(6447) != 0;
        }

        varbit6447 = newVarbit6447;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            if (isInNyloRegion()) {
                startupNyloOverlay();

                if(config.hidePillars() == SpoonTobConfig.hidePillarsMode.PILLARS){
                    removeGameObjectsFromScene(ImmutableSet.of(32862), 0);
                }else if(config.hidePillars() == SpoonTobConfig.hidePillarsMode.CLEAN){
                    removeGameObjectsFromScene(ImmutableSet.of(32862, 32876, 32899), 0);
                }

                if(config.hideEggs()){
                    removeGameObjectsFromScene(ImmutableSet.of(32939, 32937, 2739, 32865), 0);
                }
            } else {
                if (!nyloSelectionManager.isHidden() || !nylocasAliveCounterOverlay.isHidden()) {
                    shutdownNyloOverlay();
                }

                resetNylo();
                isInstanceTimerRunning = false;
            }

            nextInstance = true;
        }

    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (nyloActive) {
            if (skipTickCheck) {
                skipTickCheck = false;
            } else {
                if (client.getLocalPlayer() == null || client.getLocalPlayer().getPlayerComposition() == null) {
                    return;
                }
                int equippedWeapon = ObjectUtils.defaultIfNull(client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON), -1);
                weaponStyle = WeaponMap.StyleMap.get(equippedWeapon);
            }

            if(waveSpawnTicks >= 0){
                waveSpawnTicks--;
                if(waveSpawnTicks < 0 && nylocasAliveCounterOverlay.getNyloAlive() >= nylocasAliveCounterOverlay.getMaxNyloAlive()){
                    waveSpawnTicks = 3;
                    stalledWave = true;
                }
            }
            meleeNyloRaveColors.clear();
            rangeNyloRaveColors.clear();
            mageNyloRaveColors.clear();

            for (Iterator<NPC> it = nylocasNpcs.keySet().iterator(); it.hasNext();)
            {
                NPC npc = it.next();
                int ticksLeft = nylocasNpcs.get(npc);

                if (ticksLeft < 0)
                {
                    it.remove();
                    continue;
                }
                nylocasNpcs.replace(npc, ticksLeft - 1);

                if(npc.getId() == 8342 || npc.getId() == 8345 || npc.getId() == 8348 || npc.getId() == 8351
                        || npc.getId() == 10774 || npc.getId() == 10777 || npc.getId() == 10780 || npc.getId() == 10783
                        || npc.getId() == 10791 || npc.getId() == 10794 || npc.getId() == 10797 || npc.getId() == 10800){
                    meleeNyloRaveColors.add(Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F));
                }else if(npc.getId() == 8343 || npc.getId() == 8346 || npc.getId() == 8349 || npc.getId() == 8352
                        || npc.getId() == 10775 || npc.getId() == 10778 || npc.getId() == 10781 || npc.getId() == 10784
                        || npc.getId() == 10792 || npc.getId() == 10795 || npc.getId() == 10798 || npc.getId() == 10801){
                    rangeNyloRaveColors.add(Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F));
                }else if(npc.getId() == 8344 || npc.getId() == 8347 || npc.getId() == 8350 || npc.getId() == 8353
                        || npc.getId() == 10776 || npc.getId() == 10779 || npc.getId() == 10782 || npc.getId() == 10785
                        || npc.getId() == 10793 || npc.getId() == 10796 || npc.getId() == 10799 || npc.getId() == 10802){
                    mageNyloRaveColors.add(Color.getHSBColor(new Random().nextFloat(), 1.0F, 1.0F));
                }
            }

            for (NPC pillar : nylocasPillars.keySet())
            {
                int healthPercent = pillar.getHealthRatio();
                if (healthPercent > -1)
                {
                    nylocasPillars.replace(pillar, healthPercent);
                }
            }

            boolean foundPillar = false;
            for (NPC npc : this.client.getNpcs()) {
                if (npc.getId() == NPCID_NYLOCAS_PILLAR || npc.getId() == NPCID_NYLOCAS_SM_PILLAR || npc.getId() == NPCID_NYLOCAS_HM_PILLAR) {
                    foundPillar = true;
                    break;
                }
            }
            NPC minNPC = null;
            int minHealth = 100;
            if (foundPillar) {
                for (NPC npc : this.nylocasPillars.keySet()) {
                    int health = (npc.getHealthRatio() > -1) ? npc.getHealthRatio() : this.nylocasPillars.get(npc);
                    this.nylocasPillars.replace(npc, health);
                    if (health < minHealth) {
                        minHealth = health;
                        minNPC = npc;
                    }
                }
                if (minNPC != null && this.config.showLowestPillar() && showHint)
                    this.client.setHintArrow(minNPC);
            } else {
                this.nylocasPillars.clear();
            }

            if ((instanceTimer + 1) % 4 == 1 && nyloWave < NylocasWave.MAX_WAVE && ticksSinceLastWave < 2) {
                if (config.nyloStallMessage() && nylocasAliveCounterOverlay.getNyloAlive() >= nylocasAliveCounterOverlay.getMaxNyloAlive()) {
                    client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Stalled wave: <col=FF0000>" + nyloWave + " </col>Time:<col=FF0000> "
                            + nylocasAliveCounterOverlay.getFormattedTime() + " </col>Nylos alive:<col=FF0000> " + nylocasAliveCounterOverlay.getNyloAlive() + "/"
                            + nylocasAliveCounterOverlay.getMaxNyloAlive(), "", false);
                }
            }

            ticksSinceLastWave = Math.max(0, ticksSinceLastWave - 1);

			if (nylocasBoss != null && nyloBossAlive) {
				bossChangeTicks--;
				if (nylocasBoss.getId() != lastBossId) {
					lastBossId = nylocasBoss.getId();
					if(nylocasBoss.getId() == 10787 || nylocasBoss.getId() == 10788 || nylocasBoss.getId() == 10789){
						bossChangeTicks = 15;
					}else {
						bossChangeTicks = 10;
					}
				}
			}else if(minibossAlive && nyloMiniboss != null){
                bossChangeTicks--;
            }

            if (!splitsMap.isEmpty())
            {
                splitsMap.values().removeIf((value) -> value <= 1);
                splitsMap.replaceAll((key, value) -> value - 1);
            }
        }

        instanceTimer = (instanceTimer + 1) % 4;
    }

    @Subscribe
    protected void onClientTick(ClientTick event) {
        /*if (client.isMirrored() && !mirrorMode) {
            nylocasOverlay.setLayer(OverlayLayer.AFTER_MIRROR);
            overlayManager.remove(nylocasOverlay);
            overlayManager.add(nylocasOverlay);
            nyloTimer.setLayer(OverlayLayer.AFTER_MIRROR);
            nyloSelectionManager.setLayer(OverlayLayer.AFTER_MIRROR);
            nylocasAliveCounterOverlay.setLayer(OverlayLayer.AFTER_MIRROR);
            mirrorMode = true;
        }*/

        List<Player> players = client.getPlayers();
        for (Player player : players)
        {
            if (player.getWorldLocation() != null)
            {
                LocalPoint lp = player.getLocalLocation();

                WorldPoint wp = WorldPoint.fromRegion(player.getWorldLocation().getRegionID(), 5, 33, 0);
                LocalPoint lp1 = LocalPoint.fromWorld(client, wp.getX(), wp.getY());
                if (lp1 != null)
                {
                    Point base = new Point(lp1.getSceneX(), lp1.getSceneY());
                    Point point = new Point(lp.getSceneX() - base.getX(), lp.getSceneY() - base.getY());

                    if (isInBloatRegion() && point.getX() == -1 && (point.getY() == -1 || point.getY() == -2 || point.getY() == -3) && nextInstance)
                    {
                        client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Nylo instance timer started.", "");
                        instanceTimer = 3;
                        isInstanceTimerRunning = true;
                        nextInstance = false;
                    }
                }
            }
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        if (!bigNylos.isEmpty() && event.getActor() instanceof NPC) {
            NPC npc = (NPC) event.getActor();
            int anim = npc.getAnimation();
            if (bigNylos.contains(npc)) {
                if (anim == 8005 || anim == 7991 || anim == 7998) {
                    splitsMap.putIfAbsent(npc, 6);
                    bigNylos.remove(npc);
                }
                if (anim == 8006 || anim == 7992 || anim == 8000) {
                    splitsMap.putIfAbsent(npc, 4);
                    bigNylos.remove(npc);
                }
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event){
        String mes = event.getMessage();
        if (mes.contains("Wave 'The Nylocas'") && mes.contains("complete!<br>Duration: <col=ff0000>")){
            if ((config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.WAVES || config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOTH)
                    && config.splitMsgTiming() == SpoonTobConfig.splitsMsgTiming.FINISHED){
                if (config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.CAP || config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH){
                    client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Pre-cap splits: <col=00FFFF>" + preMageSplits + "</col> - <col=00FF00>"
                            + preRangeSplits + "</col> - <col=ff0000>" + preMeleeSplits + "</col> Post-cap splits: <col=00FFFF>" + postMageSplits + "</col> - <col=00FF00>"
                            + postRangeSplits + "</col> - <col=ff0000>" + postMeleeSplits, null);
                } if (config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.TOTAL || config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH)
                    client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Small splits: <col=00FFFF>" + mageSplits + "</col> - <col=00FF00>"
                            + rangeSplits + "</col> - <col=ff0000>" + meleeSplits + "</col> ", null);
            }
            if (config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOSS || config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOTH){
                client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Boss phases: <col=00FFFF>" + mageBoss + "</col> - <col=00FF00>"
                        + rangeBoss + "</col> - <col=ff0000>" + meleeBoss + "</col> ", null);
            }
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuAction() == MenuAction.ITEM_SECOND_OPTION) {
            WeaponStyle newStyle = WeaponMap.StyleMap.get(event.getId());
            if (newStyle != null) {
                skipTickCheck = true;
                weaponStyle = newStyle;
            }
        } else if ((config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOSS || config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOTH)
                && nylocasBoss != null && event.getMenuTarget().contains(BOSS_NYLO) && event.getMenuAction() == MenuAction.NPC_SECOND_OPTION && weaponStyle != null) {
            switch (weaponStyle) {
                case MAGIC:
                    if (nylocasBoss.getId() != NpcID.NYLOCAS_VASILIAS_8356 && nylocasBoss.getId() != 10788 && nylocasBoss.getId() != 10809) {
                       event.consume();
                    }
                    break;
                case MELEE:
                    if (nylocasBoss.getId() != NpcID.NYLOCAS_VASILIAS_8355 && nylocasBoss.getId() != 10787 && nylocasBoss.getId() != 10808) {
                        event.consume();
                    }
                    break;
                case RANGE:
                    if (nylocasBoss.getId() != NpcID.NYLOCAS_VASILIAS_8357 && nylocasBoss.getId() != 10789 && nylocasBoss.getId() != 10810) {
                        event.consume();
                    }
                    break;
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded entry) {
        if (nyloActive) {
            String target = entry.getTarget();

            if (config.nyloRecolorMenu() && (entry.getType() == MenuAction.NPC_SECOND_OPTION.getId() || entry.getType() == MenuAction.SPELL_CAST_ON_NPC.getId())) {
                MenuEntry[] entries = client.getMenuEntries();
                MenuEntry toEdit = entries[entries.length - 1];

                String strippedTarget = Text.removeTags(target);
                boolean isBig = false;
                int timeAlive;
                String timeAliveString = "";

                NPC npc = client.getCachedNPCs()[toEdit.getIdentifier()];
                if (npc != null && npc.getComposition() != null) {
                    isBig = npc.getComposition().getSize() > 1;
                    if (config.nyloTicksMenu() && nylocasNpcs.get(npc) != null) {
                        if (config.nyloTimeAliveCountStyle() == SpoonTobConfig.nylotimealive.COUNTUP) {
                            timeAlive = 52 - nylocasNpcs.get(npc);
                            timeAliveString = ColorUtil.prependColorTag(" - " + timeAlive, new Color(255 * timeAlive / 52, 255 * (52 - timeAlive) / 52, 0));
                        } else {
                            timeAlive = nylocasNpcs.get(npc);
                            timeAliveString = ColorUtil.prependColorTag(" - " + timeAlive, new Color(255 * (52 - timeAlive) / 52, 255 * timeAlive / 52, 0));
                        }
                    }
                }

                if (strippedTarget.contains(MAGE_NYLO)) {
                    if (isBig) {
                        toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 190, 190)) + timeAliveString);
                    } else {
                        toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 255, 255)) + timeAliveString);
                    }
                } else if (strippedTarget.contains(MELEE_NYLO)) {
                    if (isBig) {
                        toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(190, 150, 150)) + timeAliveString);
                    } else {
                        toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(255, 188, 188)) + timeAliveString);
                    }
                } else if (strippedTarget.contains(RANGE_NYLO)) {
                    if (isBig) {
                        toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 190, 0)) + timeAliveString);
                    } else {
                        toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 255, 0)) + timeAliveString);
                    }
                }
                client.setMenuEntries(entries);
            }

            if ((config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.WAVES || config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOTH)
                    && entry.getType() == MenuAction.NPC_SECOND_OPTION.getId() && weaponStyle != null) {
                switch (weaponStyle) {
                    case MAGIC:
                        if (target.contains(MELEE_NYLO) || target.contains(RANGE_NYLO)) {
                            client.setMenuOptionCount(client.getMenuOptionCount() - 1);
                        }
                        break;
                    case MELEE:
                        if (target.contains(RANGE_NYLO) || target.contains(MAGE_NYLO)) {
                            client.setMenuOptionCount(client.getMenuOptionCount() - 1);
                        }
                        break;
                    case RANGE:
                        if (target.contains(MELEE_NYLO) || target.contains(MAGE_NYLO)) {
                            client.setMenuOptionCount(client.getMenuOptionCount() - 1);
                        }
                        break;
                    case CHINS:
                        if (!config.ignoreChins() && (target.contains(MELEE_NYLO) || target.contains(MAGE_NYLO))) {
                            client.setMenuOptionCount(client.getMenuOptionCount() - 1);
                        }
                        break;
                }
            }

            if ((config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOSS || config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOTH)
                    && nyloMiniboss != null && target.contains(DEMIBOSS_NYLO) && entry.getType() == MenuAction.NPC_SECOND_OPTION.getId() && weaponStyle != null) {
                switch (weaponStyle) {
                    case MAGIC:
                        if (nyloMiniboss.getId() != NpcID.NYLOCAS_PRINKIPAS_10805) {
                            client.setMenuOptionCount(client.getMenuOptionCount() - 1);
                        }
                        break;
                    case MELEE:
                        if (nyloMiniboss.getId() != NpcID.NYLOCAS_PRINKIPAS_10804) {
                            client.setMenuOptionCount(client.getMenuOptionCount() - 1);
                        }
                        break;
                    case RANGE:
                        if (nyloMiniboss.getId() != NpcID.NYLOCAS_PRINKIPAS_10806) {
                            client.setMenuOptionCount(client.getMenuOptionCount() - 1);
                        }
                        break;
                }
            }
        }
    }

    static String stripColor(String str) {
        return str.replaceAll("(<col=[0-9a-f]+>|</col>)", "");
    }

    @Subscribe
    public void onMenuOpened(MenuOpened menu) {
        if (config.nyloRecolorMenu() && nyloActive && !nyloBossAlive) {
            client.setMenuEntries(Arrays.stream(menu.getMenuEntries()).filter((s) -> !s.getOption().equals("Examine")).toArray(MenuEntry[]::new));
        }
    }

    public void removeGameObjectsFromScene(Set<Integer> objectIDs, int plane) {
        Scene scene = client.getScene();
        Tile[][] tiles = scene.getTiles()[plane];
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                Tile tile = tiles[x][y];
                if (tile != null) {
                    if (objectIDs != null) {
                        Arrays.stream(tile.getGameObjects()).filter(obj -> (obj != null && objectIDs.contains(obj.getId()))).findFirst().ifPresent(scene::removeGameObject);
                    }
                }
            }
        }
    }

    boolean isInNyloRegion() {
        return client.isInInstancedRegion() && client.getMapRegions().length > 0 && client.getMapRegions()[0] == 13122;
    }

    private boolean isInBloatRegion() {
        return client.isInInstancedRegion() && client.getMapRegions().length > 0 && client.getMapRegions()[0] == 13125;
    }
}
