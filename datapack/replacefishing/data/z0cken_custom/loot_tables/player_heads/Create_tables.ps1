$Heads = Import-Excel Heads.xlsx

$JsonHead='{
    "pools": [
        {
            "rolls": 1,
            "entries": ['

$TemplateEntrie = '{
    "type": "minecraft:item",
    "name": "minecraft:player_head",
    "weight": NAME_WEIGHT,
    "functions": [
      {
        "function": "minecraft:set_name",
        "name": {
          "bold": true,
          "italic": false,
          "color": "NAME_FARBE",
          "text": "NAME_BESCHREIBUNG"
        }
      },
      {
        "function": "minecraft:set_nbt",
        "tag": "{SkullOwner:{Id:[NAME_SKULLOWNER],Properties:{textures:[{Value:\"NAME_VALUE\"},]}}}"
      }
    ]
  }
'
$JsonFoot=']
}
]
}'


$Files = $Heads.Pool | Get-Unique

foreach($File in $Files){
    Write-Verbose "$File is beeing processed"
    
    $ToProcess = $Heads | Where-Object "Pool" -eq $File
    $Json = $JsonHead
    $i = 0
    foreach($Process in $ToProcess){
        if($i-gt0){
            $Json += ","
        }
        $ToAdd = $TemplateEntrie -replace "NAME_WEIGHT",$Process.weight
        $ToAdd = $ToAdd -replace "NAME_FARBE",$Process.FARBE
        $ToAdd = $ToAdd -replace "NAME_BESCHREIBUNG",$Process.BESCHREIBUNG
        $ToAdd = $ToAdd -replace "NAME_SKULLOWNER",$Process.SkullOwner
        $ToAdd = $ToAdd -replace "NAME_VALUE",$Process.Value
        $Json += $ToAdd
        $i++
    }
    $Json += $JsonFoot

    $Json | Out-File "$($File).json" -Encoding utf8

}