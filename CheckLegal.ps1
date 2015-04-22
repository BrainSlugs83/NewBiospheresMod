try
{
	cd src
	gci -recu -inc "*.java" | ? { -not ([string]::Concat((Get-Content $_)).Contains("Sam Hocevar")); }
}
finally
{
	cd ..
}