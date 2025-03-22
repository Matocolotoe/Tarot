package fr.giovanni75.tarot.objects;

import com.google.gson.JsonObject;
import fr.giovanni75.tarot.enums.Handful;
import fr.giovanni75.tarot.enums.Misery;
import fr.giovanni75.tarot.enums.Side;

public record LocalPlayer(int id, Side side, Handful handful, Misery misery) implements Serializable {

	@Override
	public boolean equals(Object object) {
		return object instanceof LocalPlayer && id == ((LocalPlayer) object).id;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("id", id);
		if (side != Side.DEFENSE)
			object.addProperty("side", side.toString());
		if (handful != Handful.NONE)
			object.addProperty("handful", handful.name());
		if (misery != Misery.NONE)
			object.addProperty("misery", misery.name());
		return object;
	}

}
