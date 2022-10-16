cs2031_protocol = Proto("CS2031", "Liam's Protocol")
packet_type = ProtoField.uint8("cs2031.packet_type", "PacketType", base.DEC)
source_idx = ProtoField.uint8("cs2031.source_idx", "SourceIdx", base.DEC)
data_payload = ProtoField.string("cs2031.data_payload", "DataPayload")

cs2031_protocol.fields = { packet_type, source_idx, data_payload }

function get_packet_type(type)
    local type_name = "unknown"
    if type == 0 then type_name = "FILEREQ"
    elseif type == 1 then type_name = "FWDFILEREQ"
    elseif type == 2 then type_name = "FILERES"
    elseif type == 3 then type_name = "TESTPKT"
    elseif type == 4 then type_name = "REGCLIENT"
    elseif type == 5 then type_name = "REGWORKER"
    elseif type == 6 then type_name = "REGACK"
    end
    return type_name
end

function cs2031_protocol.dissector(buffer, pinfo, tree)
    local length = buffer:len()
    if length == 0 then return end

    pinfo.cols.protocol = cs2031_protocol.name
    local subtree = tree:add(cs2031_protocol, buffer(), "LJ Protocol Data")

    local type = buffer(0, 1):le_uint()
    local type_name = get_packet_type(type)

    subtree:add_le(packet_type, buffer(0, 1)):append_text(" (" .. type_name .. ")")


    if (type_name == "FILERES" or type_name == "FWDFILEREQ") then
        subtree:add_le(source_idx, buffer(1, 1))
    end

    if (length > 2) then
        local content = buffer(2, length - 2):string()
        subtree:add_le(data_payload, buffer(2, string.len(content)))
    end
end

local udp_port = DissectorTable.get('udp.port')
udp_port:add(50000, cs2031_protocol)
