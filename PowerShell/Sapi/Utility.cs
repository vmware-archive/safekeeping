using System;
using System.Collections.Generic; 
using System.Text;
using System.Text.RegularExpressions; 

namespace SapiCli
{
    static class Utility
    {
        private readonly static Regex IP4PATTERN = new Regex("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        private readonly static Regex UUIDPATTERN = new Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        public static fcoTarget[] getFcoTargets(String fcoName)
        {
            String st = fcoName.Trim();
            var res = new List<fcoTarget>();
            String searchType = null;
            int index = 0;
            while (index < st.Length)
            {
                if (searchType == null)
                {
                    if (st[index] == 'v')
                    {
                        if (st[index + 1] == 'm')
                        {
                            if (st[index + 2] == ':')
                            {
                                index += 3;
                                searchType = "vm";
                            }
                        }
                        else if ((st[index + 1] == 'a') && (st[index + 2] == 'p')
                              && (st[index + 3] == 'p') && (st[index + 4] == ':'))
                        {
                            index += 5;
                            searchType = "vapp";
                        }
                        else
                        {
                            ++index;
                        }
                    }
                    else if ((st[index] == 'i') && (st[index + 1] == 'v') && (st[index + 2] == 'd')
                          && (st[index + 3] == ':'))
                    {
                        index += 4;
                        searchType = "ivd";
                    }
                    else if ((st[index] == 't') && (st[index + 1] == 'a') && (st[index + 2] == 'g')
                          && (st[index + 3] == ':'))
                    {
                        index += 4;
                        searchType = "tag";
                    }
                    else
                    {
                        throw new SapiException("fcoName :" + st + " is not valid");
                    }
                }
                else
                {
                    var fcoValue = new StringBuilder();
                    char endFco = (st[index] == '"') ? '"' : ' ';
                    if (endFco == '"')
                    {
                        ++index;
                    }
                    while (index < st.Length)
                    {
                        if ((st[index] != endFco))
                        {
                            fcoValue.Append(st[index]);
                            ++index;
                        }
                        else
                        {
                            ++index;
                            break;
                        }
                    }

                    fcoTypeSearch typeVm;
                    String key = fcoValue.ToString();
                    switch (searchType)
                    {
                        case "ivd":
                            if (UUIDPATTERN.IsMatch(key))
                            {
                                typeVm = fcoTypeSearch.IVD_UUID;
                            }
                            else
                            {
                                typeVm = fcoTypeSearch.IVD_NAME;
                            }
                            break;
                        case "vapp":
                            if (key.StartsWith("resgroup-"))
                            {
                                typeVm = fcoTypeSearch.VAPP_MOREF;
                            }
                            else if (UUIDPATTERN.IsMatch(key))
                            {
                                typeVm = fcoTypeSearch.VAPP_UUID;
                            }
                            else
                            {
                                typeVm = fcoTypeSearch.VAPP_NAME;
                            }
                            break;
                        case "vm":
                            if (UUIDPATTERN.IsMatch(key))
                            {
                                typeVm = fcoTypeSearch.VM_UUID;
                            }
                            else if (IP4PATTERN.IsMatch(key))
                            {
                                typeVm = fcoTypeSearch.VM_IP;
                            }
                            else if (key.StartsWith("vm-"))
                            {
                                typeVm = fcoTypeSearch.VM_MOREF;
                            }
                            else
                            {
                                typeVm = fcoTypeSearch.VM_NAME;
                            }
                            break;

                        case "tag":
                            typeVm = fcoTypeSearch.TAG;
                            break;
                        default:
                            throw new SapiException("Unsupported type");
                    }
                    var FcoTarget = new fcoTarget();
                    FcoTarget.key = key;
                    FcoTarget.keyType = typeVm;
                    FcoTarget.keyTypeSpecified = true;
                    res.Add(FcoTarget);
                    searchType = null;

                }
            }
            return res.ToArray();
        }
    }
}
