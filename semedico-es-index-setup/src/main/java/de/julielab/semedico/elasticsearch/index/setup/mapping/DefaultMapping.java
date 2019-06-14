package de.julielab.semedico.elasticsearch.index.setup.mapping;

public class DefaultMapping {
	public static class All {
		public All(boolean enabled) {
			super();
			this.enabled = enabled;
		}

		public boolean enabled;
	}
	
	public static class Source {
		public Source(boolean enabled) {
			super();
			this.enabled = enabled;
		}

		public boolean enabled;
	}
	
	public All _all;
	public DefaultMapping(All _all, Source _source) {
		super();
		this._all = _all;
		this._source = _source;
	}
	public Source _source;
}
