package parser;

class NamePosition {
    private long lineOffset;
    private long charOffset;

    public NamePosition(long lineOffSet, long charOffSet) {
        lineOffset = lineOffSet;
        charOffset = charOffSet;
    }

    @Override
    public String toString() {
        return String.format("[lineOffset=%d, charOffset=%d]", lineOffset, charOffset);
    }
}
