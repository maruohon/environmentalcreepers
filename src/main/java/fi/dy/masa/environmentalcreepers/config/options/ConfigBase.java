package fi.dy.masa.environmentalcreepers.config.options;

import javax.annotation.Nullable;

public abstract class ConfigBase implements IConfigBase
{
    private final String name;
    private final String prettyName;
    private String comment;

    public ConfigBase(String name, String comment)
    {
        this(name, comment, name);
    }

    public ConfigBase(String name, String comment, String prettyName)
    {
        this.name = name;
        this.prettyName = prettyName;
        this.comment = comment;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getPrettyName()
    {
        return this.prettyName;
    }

    @Override
    @Nullable
    public String getComment()
    {
        return this.comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
