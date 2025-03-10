package fr.giovanni75.tarot.objects;

import com.google.gson.JsonObject;
import fr.giovanni75.tarot.enums.Handful;
import fr.giovanni75.tarot.enums.Misery;
import fr.giovanni75.tarot.enums.Side;

import java.util.UUID;

public record LocalPlayer(UUID uuid, Side side, Handful handful, Misery misery) implements Serializable {

	@Override
	public boolean equals(Object object) {
		return object instanceof LocalPlayer && uuid.equals(((LocalPlayer) object).uuid);
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("uuid", uuid.toString());
		if (side != Side.DEFENSE)
			object.addProperty("side", side.toString());
		if (handful != Handful.NONE)
			object.addProperty("handful", handful.name());
		if (misery != Misery.NONE)
			object.addProperty("misery", misery.name());
		return object;
	}

}
