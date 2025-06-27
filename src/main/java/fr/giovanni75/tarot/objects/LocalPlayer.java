package fr.giovanni75.tarot.objects;

import com.google.gson.JsonObject;
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
			object.addProperty("side", side.toString());
		if (handful != null)
			object.addProperty("type", handful.name());
		if (misery != null)
			object.addProperty("type", misery.name());
		return object;
	}

}
