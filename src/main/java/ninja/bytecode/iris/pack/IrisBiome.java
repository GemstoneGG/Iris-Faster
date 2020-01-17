package ninja.bytecode.iris.pack;

import java.lang.reflect.Field;

import org.bukkit.Material;
import org.bukkit.block.Biome;

import ninja.bytecode.iris.Iris;
import ninja.bytecode.iris.controller.PackController;
import ninja.bytecode.iris.util.MB;
import ninja.bytecode.iris.util.PolygonGenerator;
import ninja.bytecode.shuriken.collections.GList;
import ninja.bytecode.shuriken.collections.GMap;
import ninja.bytecode.shuriken.execution.J;
import ninja.bytecode.shuriken.json.JSONArray;
import ninja.bytecode.shuriken.json.JSONObject;
import ninja.bytecode.shuriken.logging.L;
import ninja.bytecode.shuriken.math.CNG;
import ninja.bytecode.shuriken.math.M;
import ninja.bytecode.shuriken.math.RNG;

public class IrisBiome
{
	public static final double MAX_HEIGHT = 0.77768;
	public static final double IDEAL_HEIGHT = 0.0527;
	public static final double MIN_HEIGHT = -0.0218;

	//@builder
	private static final IrisBiome OCEAN = new IrisBiome("Ocean", Biome.OCEAN)
			.height(-0.4)
			.coreBiome()
			.surface(MB.of(Material.SAND), MB.of(Material.SAND), MB.of(Material.SAND), MB.of(Material.CLAY), MB.of(Material.GRAVEL))
			.simplexSurface();
	private static final IrisBiome FROZEN_OCEAN = new IrisBiome("Frozen Ocean", Biome.FROZEN_OCEAN)
			.height(-0.4)
			.coreBiome()
			.surface(MB.of(Material.SAND), MB.of(Material.SAND), MB.of(Material.SAND), MB.of(Material.CLAY), MB.of(Material.GRAVEL))
			.simplexSurface();
	private static final IrisBiome DEEP_OCEAN = new IrisBiome("Deep Ocean", Biome.DEEP_OCEAN)
			.height(-0.6)
			.coreBiome()
			.surface(MB.of(Material.SAND), MB.of(Material.CLAY), MB.of(Material.GRAVEL))
			.simplexSurface();

	//@done
	private static final GMap<Biome, IrisBiome> map = build();
	private String name;
	private Biome realBiome;
	private double height;
	private double amp;
	private GList<MB> rock;
	private int rockDepth;
	private GList<MB> surface;
	private GList<MB> dirt;
	private GMap<MB, Double> scatterChance;
	private boolean scatterSurface;
	private boolean scatterSurfaceRock;
	private boolean scatterSurfaceSub;
	private boolean core;
	private int dirtDepth;
	private double surfaceScale;
	private double subSurfaceScale;
	private double rockScale;
	private boolean simplexScatter;
	private boolean simplexScatterRock;
	private boolean simplexScatterSub;
	private double snow;
	private double cliffChance;
	private double cliffScale;
	private boolean cliffs;
	private String region;
	private GMap<String, Double> schematicGroups;
	private PolygonGenerator.EnumPolygonGenerator<MB> poly;
	private PolygonGenerator.EnumPolygonGenerator<MB> polySub;
	private PolygonGenerator.EnumPolygonGenerator<MB> polyRock;

	public static double getMaxHeight()
	{
		return MAX_HEIGHT;
	}

	public static double getIdealHeight()
	{
		return IDEAL_HEIGHT;
	}

	public static double getMinHeight()
	{
		return MIN_HEIGHT;
	}

	public static IrisBiome getOcean()
	{
		return OCEAN;
	}

	public static IrisBiome getDeepOcean()
	{
		return DEEP_OCEAN;
	}

	public static GMap<Biome, IrisBiome> getMap()
	{
		return map;
	}

	public boolean isScatterSurface()
	{
		return scatterSurface;
	}

	public boolean isCore()
	{
		return core;
	}

	public boolean isSimplexScatter()
	{
		return simplexScatter;
	}

	public PolygonGenerator.EnumPolygonGenerator<MB> getPoly()
	{
		return poly;
	}

	public IrisBiome(JSONObject json)
	{
		this("Loading", Biome.OCEAN);
		fromJSON(json);
	}

	public IrisBiome(String name, Biome realBiome)
	{
		this.region = "default";
		this.core = false;
		this.name = name;
		cliffs = false;
		cliffScale = 1;
		cliffChance = 0.37;
		dirtDepth = 2;
		this.realBiome = realBiome;
		this.height = IDEAL_HEIGHT;
		this.amp = 0.31;
		rockDepth = 11;
		surfaceScale = 1;
		subSurfaceScale = 1;
		rockScale = 1;
		simplexScatterRock = false;
		scatterSurfaceRock = true;
		simplexScatterSub = false;
		scatterSurfaceSub = true;
		scatterChance = new GMap<>();
		schematicGroups = new GMap<>();
		//@builder
		surface(new MB(Material.GRASS))
		.dirt(new MB(Material.DIRT), new MB(Material.DIRT, 1))
		.rock(MB.of(Material.STONE),
			MB.of(Material.STONE),
			MB.of(Material.STONE),
			MB.of(Material.STONE),
			MB.of(Material.STONE),
			MB.of(Material.STONE),
			MB.of(Material.STONE, 5),
			MB.of(Material.STONE, 5),
			MB.of(Material.COBBLESTONE),
			MB.of(Material.COBBLESTONE),
			MB.of(Material.SMOOTH_BRICK),
			MB.of(Material.SMOOTH_BRICK, 1),
			MB.of(Material.SMOOTH_BRICK, 2),
			MB.of(Material.SMOOTH_BRICK, 3));
		//@done
	}

	public void fromJSON(JSONObject o)
	{
		fromJSON(o, true);
	}

	public void seal(RNG rng)
	{
		if(simplexScatter)
		{
			poly = new PolygonGenerator.EnumPolygonGenerator<MB>(rng, 0.125, 2, getSurface().toArray(new MB[getSurface().size()]), (g) ->
			{
				return g.scale(0.09 * surfaceScale).fractureWith(new CNG(rng.nextParallelRNG(56), 1D, 2).scale(0.0955), 55);
			});
		}

		else
		{
			poly = new PolygonGenerator.EnumPolygonGenerator<MB>(rng, 15.05, 2, getSurface().toArray(new MB[getSurface().size()]), (g) ->
			{
				return g.scale(surfaceScale).fractureWith(new CNG(rng.nextParallelRNG(55), 1D, 2).scale(0.0155), 224);
			});
		}

		if(simplexScatterSub)
		{
			polySub = new PolygonGenerator.EnumPolygonGenerator<MB>(rng, 0.125, 2, getDirt().toArray(new MB[getDirt().size()]), (g) ->
			{
				return g.scale(0.06 * subSurfaceScale).fractureWith(new CNG(rng.nextParallelRNG(526), 1D, 2).scale(0.0955), 55);
			});
		}

		else
		{
			polySub = new PolygonGenerator.EnumPolygonGenerator<MB>(rng, 15.05, 2, getDirt().toArray(new MB[getDirt().size()]), (g) ->
			{
				return g.scale(subSurfaceScale).fractureWith(new CNG(rng.nextParallelRNG(515), 1D, 2).scale(0.0155), 224);
			});
		}

		if(simplexScatterRock)
		{
			polyRock = new PolygonGenerator.EnumPolygonGenerator<MB>(rng, 0.125, 2, getRock().toArray(new MB[getRock().size()]), (g) ->
			{
				return g.scale(0.08 * rockScale).fractureWith(new CNG(rng.nextParallelRNG(562), 1D, 2).scale(0.0955), 55);
			});
		}

		else
		{
			polyRock = new PolygonGenerator.EnumPolygonGenerator<MB>(rng, 15.05, 2, getRock().toArray(new MB[getRock().size()]), (g) ->
			{
				return g.scale(rockScale).fractureWith(new CNG(rng.nextParallelRNG(551), 1D, 2).scale(0.0155), 224);
			});
		}
	}

	public void fromJSON(JSONObject o, boolean chain)
	{
		name = o.getString("name");
		realBiome = Biome.valueOf(o.getString("derivative").toUpperCase().replaceAll(" ", "_"));
		J.attempt(() -> region = o.getString("region"));
		J.attempt(() -> height = o.getDouble("height"));
		J.attempt(() -> snow = o.getDouble("snow"));
		J.attempt(() -> dirtDepth = o.getInt("subSurfaceDepth"));
		J.attempt(() -> dirtDepth = o.getInt("dirtDepth"));
		J.attempt(() -> rockDepth = o.getInt("rockDepth"));
		J.attempt(() -> cliffScale = o.getDouble("cliffScale"));
		J.attempt(() -> rockScale = o.getDouble("rockScale"));
		J.attempt(() -> surfaceScale = o.getDouble("surfaceScale"));
		J.attempt(() -> subSurfaceScale = o.getDouble("subSurfaceScale"));
		J.attempt(() -> cliffChance = o.getDouble("cliffChance"));
		J.attempt(() -> cliffs = o.getBoolean("cliffs"));
		J.attempt(() -> surface = mbListFromJSON(o.getJSONArray("surface")));
		J.attempt(() -> rock = mbListFromJSON(o.getJSONArray("rock")));
		J.attempt(() -> dirt = mbListFromJSON(o.getJSONArray("subSurface")));
		J.attempt(() -> dirt = mbListFromJSON(o.getJSONArray("dirt")));
		J.attempt(() -> scatterChance = scatterFromJSON(o.getJSONArray("scatter")));
		J.attempt(() -> simplexScatter = o.getString("surfaceType").equalsIgnoreCase("simplex"));
		J.attempt(() -> scatterSurface = o.getString("surfaceType").equalsIgnoreCase("scatter"));
		J.attempt(() -> simplexScatterRock = o.getString("rockType").equalsIgnoreCase("simplex"));
		J.attempt(() -> scatterSurfaceRock = o.getString("rockType").equalsIgnoreCase("scatter"));
		J.attempt(() -> simplexScatterSub = o.getString("subSurfaceType").equalsIgnoreCase("simplex"));
		J.attempt(() -> scatterSurfaceSub = o.getString("subSurfaceType").equalsIgnoreCase("scatter"));
		J.attempt(() ->
		{
			if(Iris.settings.gen.genObjects)
			{
				schematicGroups = strFromJSON(o.getJSONArray("objects"));
			}

			else
			{
				schematicGroups = new GMap<>();
			}

			if(chain)
			{
				if(Iris.settings.gen.genObjects)
				{
					for(String i : schematicGroups.k())
					{
						Iris.getController(PackController.class).loadSchematicGroup(i);
					}
				}
			}
		});
	}

	public JSONObject toJSON()
	{
		JSONObject j = new JSONObject();
		j.put("name", name);
		J.attempt(() -> j.put("region", region));
		J.attempt(() -> j.put("derivative", realBiome.name().toLowerCase().replaceAll("_", " ")));
		J.attempt(() -> j.put("height", height));
		J.attempt(() -> j.put("snow", snow));
		J.attempt(() -> j.put("cliffs", cliffs));
		J.attempt(() -> j.put("cliffScale", cliffScale));
		J.attempt(() -> j.put("rockScale", rockScale));
		J.attempt(() -> j.put("subSurfaceScale", subSurfaceScale));
		J.attempt(() -> j.put("surfaceScale", surfaceScale));
		J.attempt(() -> j.put("cliffChance", cliffChance));
		J.attempt(() -> j.put("surface", mbListToJSON(surface)));
		J.attempt(() -> j.put("rock", mbListToJSON(rock)));
		J.attempt(() -> j.put("subSurfaceDepth", dirtDepth));
		J.attempt(() -> j.put("rockDepth", rockDepth));
		J.attempt(() -> j.put("subSurface", mbListToJSON(dirt)));
		J.attempt(() -> j.put("scatter", scatterToJson(scatterChance)));
		J.attempt(() -> j.put("surfaceType", simplexScatter ? "simplex" : scatterSurface ? "scatter" : "na"));
		J.attempt(() -> j.put("subSurfaceType", simplexScatterSub ? "simplex" : scatterSurfaceSub ? "scatter" : "na"));
		J.attempt(() -> j.put("rockType", simplexScatterRock ? "simplex" : scatterSurfaceRock ? "scatter" : "na"));
		J.attempt(() -> j.put("objects", strToJson(schematicGroups)));

		return j;
	}

	private GList<MB> mbListFromJSON(JSONArray ja)
	{
		GList<MB> mb = new GList<>();

		for(int i = 0; i < ja.length(); i++)
		{
			mb.add(MB.of(ja.getString(i)));
		}

		return mb;
	}

	private JSONArray mbListToJSON(GList<MB> mbs)
	{
		JSONArray ja = new JSONArray();

		for(MB i : mbs)
		{
			ja.put(i.toString());
		}

		return ja;
	}

	public IrisBiome coreBiome()
	{
		this.core = true;
		return this;
	}

	private GMap<MB, Double> scatterFromJSON(JSONArray ja)
	{
		GMap<MB, Double> mb = new GMap<MB, Double>();

		for(int i = 0; i < ja.length(); i++)
		{
			String s = ja.getString(i);
			mb.put(MB.of(s.split("\\Q=\\E")[0]), Double.valueOf(s.split("\\Q=\\E")[1]));
		}

		return mb;
	}

	private JSONArray scatterToJson(GMap<MB, Double> mbs)
	{
		JSONArray ja = new JSONArray();

		for(MB i : mbs.k())
		{
			ja.put(i.toString() + "=" + mbs.get(i));
		}

		return ja;
	}

	private GMap<String, Double> strFromJSON(JSONArray ja)
	{
		GMap<String, Double> mb = new GMap<String, Double>();

		for(int i = 0; i < ja.length(); i++)
		{
			String s = ja.getString(i);
			mb.put(s.split("\\Q=\\E")[0], Double.valueOf(s.split("\\Q=\\E")[1]));
		}

		return mb;
	}

	private JSONArray strToJson(GMap<String, Double> mbs)
	{
		JSONArray ja = new JSONArray();

		for(String i : mbs.k())
		{
			ja.put(i.toString() + "=" + mbs.get(i));
		}

		return ja;
	}

	private static GMap<Biome, IrisBiome> build()
	{
		GMap<Biome, IrisBiome> g = new GMap<Biome, IrisBiome>();

		for(Field i : IrisBiome.class.getDeclaredFields())
		{
			J.attempt(() ->
			{
				i.setAccessible(true);

				IrisBiome bb = (IrisBiome) i.get(null);

				if(!g.containsKey(bb.realBiome))
				{
					g.put(bb.realBiome, bb);
				}
			});
		}

		return g;
	}

	public IrisBiome scatter(MB mb, Double chance)
	{
		scatterChance.put(mb, chance);

		return this;
	}

	public IrisBiome schematic(String t, double chance)
	{
		schematicGroups.put(t, chance);

		return this;
	}

	public IrisBiome simplexSurface()
	{
		simplexScatter = true;
		return this;
	}

	public IrisBiome scatterSurface()
	{
		scatterSurface = true;
		return this;
	}

	public IrisBiome surface(MB... mbs)
	{
		surface = new GList<>(mbs);
		return this;
	}

	public IrisBiome dirt(MB... mbs)
	{
		dirt = new GList<>(mbs);
		return this;
	}

	public IrisBiome rock(MB... mbs)
	{
		rock = new GList<>(mbs);
		return this;
	}

	public IrisBiome height(double height)
	{
		if(height >= 0)
		{
			this.height = M.lerp(IDEAL_HEIGHT, MAX_HEIGHT, M.clip(height, 0D, 1D));
		}

		else
		{
			this.height = M.lerp(MIN_HEIGHT, IDEAL_HEIGHT, M.clip(height, -1D, 0D));
		}

		return this;
	}

	public IrisBiome amp(double amp)
	{
		this.amp = amp;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Biome getRealBiome()
	{
		return realBiome;
	}

	public double getHeight()
	{
		return height;
	}

	public double getAmp()
	{
		return amp;
	}

	public GList<MB> getSurface()
	{
		return surface;
	}

	public GList<MB> getRock()
	{
		return rock;
	}

	public GList<MB> getDirt()
	{
		return dirt;
	}

	public MB getSurface(double x, double z, RNG rng)
	{
		double wx = x + 1000D;
		double wz = z + 1000D;

		if(polySub == null)
		{
			L.w(getName() + " is not sealed!");
		}

		if(simplexScatter)
		{
			return poly.getChoice(wx / 3, wz / 3);
		}

		if(scatterSurface)
		{
			return poly.getChoice(wx * 0.2D, wz * 0.2D);
		}

		return getSurface().getRandom();
	}

	public MB getSubSurface(double x, double i, double z, RNG rng)
	{
		double wx = x + 1000D;
		double wz = z + 1000D;

		if(polySub == null)
		{
			L.w(getName() + " is not sealed!");
		}

		if(simplexScatterSub)
		{
			return polySub.getChoice(wx / 3, i / 3, wz / 3);
		}

		if(scatterSurfaceSub)
		{
			return polySub.getChoice(wx * 0.2D, i / 3, wz * 0.2D);
		}

		return getSurface().getRandom();
	}

	public MB getRock(double x, double i, double z, RNG rng)
	{
		double wx = x + 1000D;
		double wz = z + 1000D;

		if(polySub == null)
		{
			L.w(getName() + " is not sealed!");
		}

		if(simplexScatterRock)
		{
			return polyRock.getChoice(wx / 3, i / 3, wz / 3);
		}

		if(scatterSurfaceRock)
		{
			return polyRock.getChoice(wx * 0.2D, i * 0.2D, wz * 0.2D);
		}

		return getSurface().getRandom();
	}

	public GMap<MB, Double> getScatterChance()
	{
		return scatterChance;
	}

	public MB getScatterChanceSingle()
	{
		for(MB i : getScatterChance().keySet())
		{
			if(M.r(getScatterChance().get(i)))
			{
				return i;
			}
		}

		return MB.of(Material.AIR);
	}

	public static GList<IrisBiome> getBiomes()
	{
		return map.v().remove(IrisBiome.OCEAN, IrisBiome.DEEP_OCEAN);
	}

	public static GList<IrisBiome> getAllBiomes()
	{
		return map.v();
	}

	public static IrisBiome findByBiome(Biome biome)
	{
		if(map.containsKey(biome))
		{
			return map.get(biome);
		}

		return IrisBiome.OCEAN;
	}

	public GMap<String, Double> getSchematicGroups()
	{
		return schematicGroups;
	}

	public boolean isSurface(Material t)
	{
		for(MB i : surface)
		{
			if(i.material.equals(t))
			{
				return true;
			}
		}

		return false;
	}

	public String getRegion()
	{
		return region;
	}

	public boolean isSnowy()
	{
		return getSnow() > 0;
	}

	public double getSnow()
	{
		return snow;
	}

	public double getCliffScale()
	{
		return cliffScale;
	}

	public boolean hasCliffs()
	{
		return cliffs;
	}

	public int getDirtDepth()
	{
		return dirtDepth;
	}

	public int getRockDepth()
	{
		return rockDepth;
	}

	public boolean isCliffs()
	{
		return cliffs;
	}

	public double getCliffChance()
	{
		return cliffChance;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amp);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cliffChance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(cliffScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (cliffs ? 1231 : 1237);
		result = prime * result + (core ? 1231 : 1237);
		result = prime * result + ((dirt == null) ? 0 : dirt.hashCode());
		result = prime * result + dirtDepth;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((poly == null) ? 0 : poly.hashCode());
		result = prime * result + ((polyRock == null) ? 0 : polyRock.hashCode());
		result = prime * result + ((polySub == null) ? 0 : polySub.hashCode());
		result = prime * result + ((realBiome == null) ? 0 : realBiome.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((rock == null) ? 0 : rock.hashCode());
		result = prime * result + rockDepth;
		temp = Double.doubleToLongBits(rockScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((scatterChance == null) ? 0 : scatterChance.hashCode());
		result = prime * result + (scatterSurface ? 1231 : 1237);
		result = prime * result + (scatterSurfaceRock ? 1231 : 1237);
		result = prime * result + (scatterSurfaceSub ? 1231 : 1237);
		result = prime * result + ((schematicGroups == null) ? 0 : schematicGroups.hashCode());
		result = prime * result + (simplexScatter ? 1231 : 1237);
		result = prime * result + (simplexScatterRock ? 1231 : 1237);
		result = prime * result + (simplexScatterSub ? 1231 : 1237);
		temp = Double.doubleToLongBits(snow);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(subSurfaceScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((surface == null) ? 0 : surface.hashCode());
		temp = Double.doubleToLongBits(surfaceScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		IrisBiome other = (IrisBiome) obj;
		if(Double.doubleToLongBits(amp) != Double.doubleToLongBits(other.amp))
			return false;
		if(Double.doubleToLongBits(cliffChance) != Double.doubleToLongBits(other.cliffChance))
			return false;
		if(Double.doubleToLongBits(cliffScale) != Double.doubleToLongBits(other.cliffScale))
			return false;
		if(cliffs != other.cliffs)
			return false;
		if(core != other.core)
			return false;
		if(dirt == null)
		{
			if(other.dirt != null)
				return false;
		}
		else if(!dirt.equals(other.dirt))
			return false;
		if(dirtDepth != other.dirtDepth)
			return false;
		if(Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if(name == null)
		{
			if(other.name != null)
				return false;
		}
		else if(!name.equals(other.name))
			return false;
		if(poly == null)
		{
			if(other.poly != null)
				return false;
		}
		else if(!poly.equals(other.poly))
			return false;
		if(polyRock == null)
		{
			if(other.polyRock != null)
				return false;
		}
		else if(!polyRock.equals(other.polyRock))
			return false;
		if(polySub == null)
		{
			if(other.polySub != null)
				return false;
		}
		else if(!polySub.equals(other.polySub))
			return false;
		if(realBiome != other.realBiome)
			return false;
		if(region == null)
		{
			if(other.region != null)
				return false;
		}
		else if(!region.equals(other.region))
			return false;
		if(rock == null)
		{
			if(other.rock != null)
				return false;
		}
		else if(!rock.equals(other.rock))
			return false;
		if(rockDepth != other.rockDepth)
			return false;
		if(Double.doubleToLongBits(rockScale) != Double.doubleToLongBits(other.rockScale))
			return false;
		if(scatterChance == null)
		{
			if(other.scatterChance != null)
				return false;
		}
		else if(!scatterChance.equals(other.scatterChance))
			return false;
		if(scatterSurface != other.scatterSurface)
			return false;
		if(scatterSurfaceRock != other.scatterSurfaceRock)
			return false;
		if(scatterSurfaceSub != other.scatterSurfaceSub)
			return false;
		if(schematicGroups == null)
		{
			if(other.schematicGroups != null)
				return false;
		}
		else if(!schematicGroups.equals(other.schematicGroups))
			return false;
		if(simplexScatter != other.simplexScatter)
			return false;
		if(simplexScatterRock != other.simplexScatterRock)
			return false;
		if(simplexScatterSub != other.simplexScatterSub)
			return false;
		if(Double.doubleToLongBits(snow) != Double.doubleToLongBits(other.snow))
			return false;
		if(Double.doubleToLongBits(subSurfaceScale) != Double.doubleToLongBits(other.subSurfaceScale))
			return false;
		if(surface == null)
		{
			if(other.surface != null)
				return false;
		}
		else if(!surface.equals(other.surface))
			return false;
		if(Double.doubleToLongBits(surfaceScale) != Double.doubleToLongBits(other.surfaceScale))
			return false;
		return true;
	}
}
