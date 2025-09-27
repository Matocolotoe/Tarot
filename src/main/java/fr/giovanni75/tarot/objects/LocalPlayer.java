package fr.giovanni75.tarot.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.Handful;
import fr.giovanni75.tarot.enums.Misery;
import fr.giovanni75.tarot.enums.Side;

public final class LocalPlayer implements Serializable {

	public final Player player;
	public final Side side;
	public final Handful handful;
	public final Misery misery;

	int score;

	public LocalPlayer(Player player, Side side, Handful handful, Misery misery) {
		this.player = player;
		this.side = side;
		this.handful = handful;
		this.misery = misery;
	}

	LocalPlayer(JsonObject json) {
		int id = json.get("id").getAsInt();
		this.player = Tarot.getPlayer(id);
		JsonElement element = json.get("side");
		this.side = element == null ? Side.DEFENSE : Side.ALL_SIDES[element.getAsInt()];
		element = json.get("handful");
		this.handful = element == null ? null : Handful.ALL_HANDFULS[element.getAsInt() + 1];
		element = json.get("misery");
		this.misery = element == null ? null : Misery.ALL_MISERIES[element.getAsInt() + 1];
	}

	@Override
	public void edit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof LocalPlayer && getID() == ((LocalPlayer) object).getID();
	}

	public int getID() {
		return player.getID();
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("id", player.getID());
		if (side != Side.DEFENSE)
			object.addProperty("side", side.ordinal());
		if (handful != null)
			object.addProperty("handful", handful.ordinal());
		if (misery != null)
			object.addProperty("misery", misery.ordinal());
		return object;
	}

}
